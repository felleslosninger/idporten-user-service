package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.*;

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

    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        Assert.notNull(idPortenUser.getId(), "id is mandatory");
        Optional<UserEntity> user = userRepository.findByUuid(idPortenUser.getId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found for UUID: " + idPortenUser.getId());        // TODO: error handling
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

    public IDPortenUser changePid(String currentPid, String newPid) {
        Optional<UserEntity> userExists = userRepository.findByPersonIdentifier(currentPid);
        if (userExists.isEmpty()) {
            throw new IllegalArgumentException("User not found for pid:" + currentPid); // TODO: change to UserNotFoundException or something like that
        }
        if (userRepository.findByPersonIdentifier(newPid).isPresent()) {
            throw new IllegalArgumentException("User already exits for new pid:" + newPid); // TODO: change to different exception
        }
        UserEntity currentUser = userExists.get();
        UserEntity newUser = userRepository.save(UserEntity.builder().personIdentifier(newPid).active(true).previousUser(currentUser).build());

        currentUser.setActive(false);
        userRepository.save(currentUser);

        return new IDPortenUser(newUser);
    }

    public List<IDPortenUser> findUserHistory(String pid) {
        Assert.notNull(pid, "pid is mandatory");
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
        Assert.notNull(pid, "pid is mandatory");
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
