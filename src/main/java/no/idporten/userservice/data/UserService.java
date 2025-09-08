package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;

    public IDPortenUser findUser(UUID uuid) {
        String cachedUser = uuidToUseridCache.opsForValue().get(uuid.toString());

        if (cachedUser != null) {
            return searchForUser(cachedUser).orElse(null);
        }

        Optional<UserEntity> user = userRepository.findByUuid(uuid);
        if (user.isEmpty()) {
            return null;
        }

        idportenUserCache.opsForValue().set(user.get().getPersonIdentifier(), new IDPortenUser(user.get()));

        return new IDPortenUser(user.get());
    }

    public Optional<IDPortenUser> searchForUser(String personIdentifier) {
        IDPortenUser idPortenUser = idportenUserCache.opsForValue().get(personIdentifier);

        if (idPortenUser == null) {
            Optional<UserEntity> user = userRepository.findByPersonIdentifier(personIdentifier);

            if (user.isPresent()) {
                idPortenUser = new IDPortenUser(user.get());
                idportenUserCache.opsForValue().set(user.get().getPersonIdentifier(), new IDPortenUser(user.get()));
                uuidToUseridCache.opsForValue().set(user.get().getUuid().toString(), user.get().getPersonIdentifier());
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(idPortenUser);
    }

    @Transactional
    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() != null) {
            throw UserServiceException.invalidUserData("User id must be assigned by server.");
        }
        if (searchForUser(idPortenUser.getPid()).isPresent()) {
            throw UserServiceException.duplicateUser();
        }
        idPortenUser.setActive(Boolean.TRUE);
        UserEntity user = idPortenUser.toEntity();
        UserEntity userSaved = userRepository.save(user);

        idportenUserCache.opsForValue().set(userSaved.getPersonIdentifier(), new IDPortenUser(userSaved));
        uuidToUseridCache.opsForValue().set(userSaved.getUuid().toString(), userSaved.getPersonIdentifier());

        return new IDPortenUser(userSaved);
    }

    @Transactional
    public IDPortenUser createStatusUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() != null) {
            throw UserServiceException.invalidUserData("User id must be assigned by server.");
        }
        UserEntity user = idPortenUser.toEntity();

        UserEntity userSaved = userRepository.save(user);
        idportenUserCache.opsForValue().set(userSaved.getPersonIdentifier(), new IDPortenUser(userSaved));

        return new IDPortenUser(userSaved);
    }

    @Transactional
    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() == null) {
            throw UserServiceException.invalidUserData("User id is mandatory.");
        }
        Optional<UserEntity> user = userRepository.findByUuid(idPortenUser.getId());
        if (user.isEmpty()) {
            throw UserServiceException.userNotFound();
        }
        UserEntity existingUser = user.get();
        if (idPortenUser.getClosedCode() == null) {
            existingUser.setClosedCode(null);
            existingUser.setClosedCodeUpdatedAtEpochMs(0);
            existingUser.setActive(true);
        } else if (!idPortenUser.getClosedCode().isEmpty() && !idPortenUser.getClosedCode().equals(existingUser.getClosedCode())) {
            existingUser.setClosedCode(idPortenUser.getClosedCode());
            existingUser.setClosedCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
            existingUser.setActive(false);
        }
        if (!CollectionUtils.isEmpty(idPortenUser.getHelpDeskCaseReferences())) {
            existingUser.setHelpDeskCaseReferences(String.join(",", idPortenUser.getHelpDeskCaseReferences()));
        } else {
            existingUser.setHelpDeskCaseReferences(null);
        }

        UserEntity savedUser = userRepository.save(existingUser);
        idportenUserCache.opsForValue().set(savedUser.getPersonIdentifier(), new IDPortenUser(savedUser));

        return new IDPortenUser(savedUser);
    }

    @Transactional
    public IDPortenUser updateUserWithEid(UUID userUuid, Login eid) {
        Optional<UserEntity> byUuid = userRepository.findByUuid(userUuid);
        if (byUuid.isEmpty()) {
            throw UserServiceException.userNotFound();
        }
        UserEntity existingUser = byUuid.get();
        List<LoginEntity> existingEIDs = existingUser.getLogins();
        LoginEntity eidToUpdate = findExistingEid(eid, existingEIDs);

        if (eidToUpdate != null) {
            eidToUpdate.setLastLoginAtEpochMs(Instant.now().toEpochMilli());
        } else {
            LoginEntity updatedEid = LoginEntity.builder().eidName(eid.getEidName()).user(existingUser).build();
            existingEIDs.add(updatedEid); //last-login and first-login set via annotations on entity on create
        }
        UserEntity savedUser = userRepository.save(existingUser);
        idportenUserCache.opsForValue().set(savedUser.getPersonIdentifier(), new IDPortenUser(savedUser));

        return new IDPortenUser(savedUser);
    }

    private LoginEntity findExistingEid(Login eid, List<LoginEntity> existingeIDs) {
        for (LoginEntity e : existingeIDs) {
            if (e.getEidName().equalsIgnoreCase(eid.getEidName())) {
                return e;
            }
        }
        return null;
    }

    @Transactional
    public IDPortenUser deleteUser(UUID userUuid) {
        IDPortenUser user = findUser(userUuid);

        Optional<UserEntity> userExists = userRepository.findByUuid(userUuid);
        if (userExists.isEmpty()) {
            return null;
        }
        userRepository.delete(UserEntity.builder().uuid(userUuid).build());

        idportenUserCache.opsForValue().getAndDelete(user.getPid());
        uuidToUseridCache.opsForValue().getAndDelete(userUuid.toString());

        return new IDPortenUser(userExists.get());
    }

    @Transactional
    public IDPortenUser changePid(String currentPid, String newPid) {
        IDPortenUser userExists = searchForUser(currentPid).orElseThrow(() -> UserServiceException.userNotFound("No user found for current person identifier."));

        if (userRepository.findByPersonIdentifier(newPid).isPresent()) {
            throw UserServiceException.duplicateUser("User already exists for new person identifier.");
        }
        UserEntity currentUser = userExists.toEntity();
        UserEntity newUser = userRepository.save(UserEntity.builder().personIdentifier(newPid).active(true).previousUser(currentUser).build());

        currentUser.setActive(false);
        userRepository.save(currentUser);

        idportenUserCache.opsForValue().getAndDelete(currentPid);
        idportenUserCache.opsForValue().set(newUser.getPersonIdentifier(), new IDPortenUser(newUser));
        uuidToUseridCache.opsForValue().set(newUser.getUuid().toString(), newUser.getPersonIdentifier());


        return new IDPortenUser(newUser);
    }
}
