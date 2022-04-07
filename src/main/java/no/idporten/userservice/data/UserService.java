package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public IDPortenUser findUser(UUID uuid) {
        Optional<UserEntity> user = userRepository.findByUuid(uuid);
        if(user.isEmpty()){
            return null;
        }
        return new IDPortenUser(user.get());
    }

    public List<IDPortenUser> searchForUser(String personIdentifier) {
        Optional<UserEntity> users = userRepository.findByPersonIdentifier(personIdentifier);
        if(users.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        return users.stream().map(IDPortenUser::new).toList();
    }

    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        Assert.isNull(idPortenUser.getId(), "id is assigned by server");
        Assert.isTrue(searchForUser(idPortenUser.getPid()).isEmpty(), "User exists");

        UserEntity userSaved = userRepository.save(idPortenUser.toEntity());
        return new IDPortenUser(userSaved);
    }

    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        Assert.notNull(idPortenUser.getId(), "id is mandatory");
        UserEntity savedUser = userRepository.save(idPortenUser.toEntity());
        return new IDPortenUser(savedUser);
    }

    public IDPortenUser updateUserWithEid(UUID userUuid, EID eid) {
        Assert.notNull(userUuid, "userUuid is mandatory");
        Assert.notNull(eid, "eid is mandatory");
        EIDEntity eidEntity = EIDEntity.builder().name(eid.getName()).build();
        UserEntity user = UserEntity.builder().uuid(userUuid).eIDs(Collections.singletonList(eidEntity)).build();
        UserEntity savedUser = userRepository.save(user);
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
