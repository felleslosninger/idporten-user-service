package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public IDPortenUser findUser(UUID uuid) {
        Optional<UserEntity> user = userRepository.findByUuid(uuid);
        if (user.isEmpty()) {
            return null;
        }
        return new IDPortenUser(user.get());
    }

    public List<IDPortenUser> searchForUser(String personIdentifier) {
        Optional<UserEntity> users = userRepository.findByPersonIdentifier(personIdentifier);
        if (users.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return users.stream().map(IDPortenUser::new).toList();
    }

    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        Assert.isNull(idPortenUser.getId(), "id is assigned by server");
        Assert.isTrue(searchForUser(idPortenUser.getPid()).isEmpty(), "User exists");

        idPortenUser.setActive(Boolean.TRUE);
        UserEntity user = toEntity(idPortenUser);
        UserEntity userSaved = userRepository.save(user);
        return new IDPortenUser(userSaved);
    }

    public UserEntity toEntity(IDPortenUser user) {
        UserEntity.UserEntityBuilder builder = UserEntity.builder();
        builder.personIdentifier(user.getPid()).uuid(user.getId()).active(user.getActive());
        if (user.getCloseCode() != null) {
            builder.closeCode(user.getCloseCode());
            builder.closeCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (!user.getHelpDeskCaseReferences().isEmpty()) {
            builder.helpDeskCaseReferences(String.join(",", user.getHelpDeskCaseReferences()));
        }

        return builder.build();
    }

    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        Assert.notNull(idPortenUser.getId(), "id is mandatory");
        Optional<UserEntity> user = userRepository.findByUuid(idPortenUser.getId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found for UUID: " + idPortenUser.getId());        // TODO: error handling
        }
        UserEntity existingUser = user.get();
        if (idPortenUser.getActive() != null) {
            existingUser.setActive(idPortenUser.getActive());
        }
        if (idPortenUser.getCloseCode() != null && !idPortenUser.getCloseCode().equals(existingUser.getCloseCode())) {
            existingUser.setCloseCode(idPortenUser.getCloseCode());
            existingUser.setCloseCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (!idPortenUser.getHelpDeskCaseReferences().isEmpty()) {
            existingUser.setHelpDeskCaseReferences(String.join(",", idPortenUser.getHelpDeskCaseReferences()));
        }

        UserEntity savedUser = userRepository.save(existingUser);
        return new IDPortenUser(savedUser);
    }

    public IDPortenUser updateUserWithEid(UUID userUuid, EID eid) {
        Assert.notNull(userUuid, "userUuid is mandatory");
        Assert.notNull(eid, "eid is mandatory");

        Optional<UserEntity> byUuid = userRepository.findByUuid(userUuid);
        if (byUuid.isEmpty()) {
            throw new RuntimeException("User not found for UUID: " + userUuid);        // TODO: error handling
        }
        UserEntity existingUser = byUuid.get();
        List<EIDEntity> existingEIDs = existingUser.getEIDs();
        EIDEntity eidToUpdate = findExistingEid(eid, existingEIDs);

        if (eidToUpdate != null) {
            eidToUpdate.setLastLoginAtEpochMs(Instant.now().toEpochMilli());
        } else {
            EIDEntity updatedEid = EIDEntity.builder().name(eid.getName()).user(existingUser).build();
            existingEIDs.add(updatedEid); //last-login and first-login set via annotations on entity on create
        }
        UserEntity savedUser = userRepository.save(existingUser);
        return new IDPortenUser(savedUser);
    }

    private EIDEntity findExistingEid(EID eid, List<EIDEntity> existingeIDs) {
        for (EIDEntity e : existingeIDs) {
            if (e.getName().equals(eid.getName())) {
                return e;
            }
        }
        return null;
    }

    public IDPortenUser deleteUser(UUID userUuid) {
        Optional<UserEntity> userExists = userRepository.findByUuid(userUuid);
        if (userExists.isEmpty()) {
            return null;
        }
        userRepository.delete(UserEntity.builder().uuid(userUuid).build());

        return new IDPortenUser(userExists.get());
    }

}
