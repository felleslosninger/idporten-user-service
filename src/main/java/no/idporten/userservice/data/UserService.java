package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public IDPortenUser findUser(String id) {
        Optional<UserEntity> user = userRepository.findByUuid(UUID.fromString(id));
        if(user.isEmpty()){
            return null;
        }
        return convert(user.get());
    }

    public List<IDPortenUser> searchForUser(String pid) {
        Optional<UserEntity> users = userRepository.findByPersonIdentifier(pid);
        return users.stream().map(this::convert).toList();
    }

    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        Assert.isNull(idPortenUser.getId(), "id is assigned by server");
        Assert.isTrue(searchForUser(idPortenUser.getPid()).isEmpty(), "User exists");

        UserEntity userSaved = userRepository.save(copyData(idPortenUser));
        return convert(userSaved);
    }

    public IDPortenUser updateUser(String id, IDPortenUser idPortenUser) {
        Assert.notNull(id, "id is mandatory");
        Assert.isTrue(Objects.equals(id, idPortenUser.getId().toString()), "id must match resource.id");
        UserEntity savedUser = userRepository.save(copyData(idPortenUser));
        return convert(savedUser);
    }

    public IDPortenUser deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        Optional<UserEntity> userExists = userRepository.findByUuid(UUID.fromString(id));
        if (userExists.isEmpty()) {
            return null;
        }
        userRepository.delete(UserEntity.builder().uuid(uuid).build());

        return convert(userExists.get());
    }

    private IDPortenUser convert(UserEntity u) {
        return new IDPortenUser(u);
    }

    private UserEntity copyData(IDPortenUser idPortenUser) {
        return idPortenUser.toEntity();
    }

}
