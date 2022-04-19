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
        UserEntity userSaved = userRepository.save(idPortenUser.toEntity());

        return new IDPortenUser(userSaved);
    }

    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        Assert.notNull(idPortenUser.getId(), "id is mandatory");
        Optional<UserEntity> user = userRepository.findByUuid(idPortenUser.getId());
        if (user.isEmpty()) {
            return null; // todo throw exception
        }
        UserEntity existingUser = user.get();
        if(idPortenUser.getActive()!=null){
            existingUser.setActive(idPortenUser.getActive());
        }
        if(idPortenUser.getCloseCode()!=null && !idPortenUser.getCloseCode().equals(existingUser.getCloseCode())){
            existingUser.setCloseCode(idPortenUser.getCloseCode());
            existingUser.setCloseCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (!idPortenUser.getHelpDeskCaseReferences().isEmpty()) {
            existingUser.setHelpDeskCaseReferences(String.join(",", idPortenUser.getHelpDeskCaseReferences()));
        }
        if (idPortenUser.getEids() != null && !idPortenUser.getEids().isEmpty()) {
            existingUser.setEIDs(idPortenUser.getEids().stream().map(EID::toEntity).toList());
        }

        UserEntity savedUser = userRepository.save(existingUser);
        return new IDPortenUser(savedUser);
    }

    public IDPortenUser updateUserWithEid(UUID userUuid, EID eid) {
        Assert.notNull(userUuid, "userUuid is mandatory");
        Assert.notNull(eid, "eid is mandatory");

        Optional<UserEntity> byUuid = userRepository.findByUuid(userUuid);
        if (byUuid.isEmpty()) {
            throw new RuntimeException("User not found for UUID " + userUuid);        // TODO: error handling
        }
        UserEntity existingUser = byUuid.get();
        List<EIDEntity> existingeIDs = existingUser.getEIDs();
        EIDEntity eidToUpdate = null;
        for (EIDEntity e : existingeIDs) {
            if (e.getName().equals(eid.getName())) {
                eidToUpdate = e;
            }
        }
        EIDEntity updatedEid = EIDEntity.builder().name(eid.getName()).user(existingUser).build();
        if (eidToUpdate != null) {
            updatedEid.setId(eidToUpdate.getId());
            updatedEid.setFirstLoginAtEpochMs(eidToUpdate.getFirstLoginAtEpochMs());
            updatedEid.setLastLoginAtEpochMs(Instant.now().toEpochMilli());
            existingeIDs.remove(eidToUpdate);
            userRepository.save(existingUser);
        }
        existingeIDs.add(updatedEid);
        UserEntity savedUser = userRepository.save(existingUser);
        return new IDPortenUser(savedUser);
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
