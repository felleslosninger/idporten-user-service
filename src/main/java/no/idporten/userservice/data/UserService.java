package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.swing.text.html.Option;
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
        Optional<UserEntity> user = userRepository.findByUuid(uuid);
        if (user.isEmpty()) {
            return null;
        }
        return new IDPortenUser(user.get());
    }


    public Optional<IDPortenUser> searchForUser(String personIdentifier) {
        Optional<UserEntity> users = userRepository.findByPersonIdentifier(personIdentifier);
        return users.map(IDPortenUser::new);
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
        UserEntity user = toEntity(idPortenUser);
        UserEntity userSaved = userRepository.save(user);
        return new IDPortenUser(userSaved);
    }

    @Transactional
    public IDPortenUser createStatusUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() != null) {
            throw UserServiceException.invalidUserData("User id must be assigned by server.");
        }
        UserEntity user = toEntity(idPortenUser);
        UserEntity userSaved = userRepository.save(user);
        return new IDPortenUser(userSaved);
    }

    private UserEntity toEntity(IDPortenUser user) {
        UserEntity.UserEntityBuilder builder = UserEntity.builder();
        builder.personIdentifier(user.getPid()).uuid(user.getId()).active(user.isActive());
        if (user.getClosedCode() != null) {
            builder.closedCode(user.getClosedCode());
            builder.closedCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (!user.getHelpDeskCaseReferences().isEmpty()) {
            builder.helpDeskCaseReferences(String.join(",", user.getHelpDeskCaseReferences()));
        }

        return builder.build();
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
        } else if (idPortenUser.getClosedCode() != null && !idPortenUser.getClosedCode().isEmpty() && !idPortenUser.getClosedCode().equals(existingUser.getClosedCode())) {
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
        Optional<UserEntity> userExists = userRepository.findByUuid(userUuid);
        if (userExists.isEmpty()) {
            return null;
        }
        userRepository.delete(UserEntity.builder().uuid(userUuid).build());

        return new IDPortenUser(userExists.get());
    }

    @Transactional
    public IDPortenUser changePid(String currentPid, String newPid) {
        Optional<UserEntity> userExists = userRepository.findByPersonIdentifier(currentPid);
        if (userExists.isEmpty()) {
            throw UserServiceException.userNotFound("No user found for current person identifier.");
        }
        if (userRepository.findByPersonIdentifier(newPid).isPresent()) {
            throw UserServiceException.duplicateUser("User already exists for new person identifier.");
        }
        UserEntity currentUser = userExists.get();
        UserEntity newUser = userRepository.save(UserEntity.builder().personIdentifier(newPid).active(true).previousUser(currentUser).build());

        currentUser.setActive(false);
        userRepository.save(currentUser);

        return new IDPortenUser(newUser);
    }

    public List<IDPortenUser> findUserHistory(String pid) {
        Optional<UserEntity> userExists = userRepository.findByPersonIdentifier(pid);
        if (userExists.isEmpty()) {
            return null;
        }
        List<IDPortenUser> previousUsers = new ArrayList<>();
        UserEntity user = userExists.get();
        previousUsers.add(new IDPortenUser(user));

        UserEntity u = user;
        while (u != null) {
            u = findAllPreviousUsers(previousUsers, u);
        }

        return previousUsers;
    }


    public List<IDPortenUser> findUserHistoryAndNewer(String pid) {
        Optional<UserEntity> userExists = userRepository.findByPersonIdentifier(pid);
        if (userExists.isEmpty()) {
            return null;
        }
        List<IDPortenUser> allUsers = new ArrayList<>();
        UserEntity currentUser = userExists.get();


        UserEntity newUser = currentUser;
        while (newUser != null) {
            newUser = findAllNewerUsers(allUsers, newUser);
        }
        Collections.reverse(allUsers); // oldest users first in list

        allUsers.add(new IDPortenUser(currentUser));

        UserEntity oldUser = currentUser;
        while (oldUser != null) {
            oldUser = findAllPreviousUsers(allUsers, oldUser);
        }


        return allUsers;
    }

    private UserEntity findAllPreviousUsers(List<IDPortenUser> previousUsers, UserEntity user) {
        if (user.getPreviousUser() != null) {
            UserEntity previousUser = user.getPreviousUser();
            previousUsers.add(new IDPortenUser(previousUser));
            return previousUser;
        }
        return null;
    }


    private UserEntity findAllNewerUsers(List<IDPortenUser> nextUsers, UserEntity user) {
        if (user.getNextUser() != null) {
            UserEntity nextUser = user.getNextUser();
            nextUsers.add(new IDPortenUser(nextUser));
            return nextUser;
        }
        return null;
    }


}
