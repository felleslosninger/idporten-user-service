package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

//    private Map<String, IDPortenUser> userMap = new HashMap<>();

    public IDPortenUser findUser(String id) {
//        return userMap.get(id);
        Optional<UserEntity> user = userRepository.findByUuid(UUID.fromString(id));
        return convert(user.get());
    }

    public List<IDPortenUser> searchForUser(String pid) {
        Optional<UserEntity> users = userRepository.findByPersonIdentifier(pid);
        return users.stream().map(this::convert).toList();
    }

    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        Assert.isNull(idPortenUser.getId(), "id is assigned by server");
        Assert.isTrue(searchForUser(idPortenUser.getPid()).isEmpty(), "User exists");
//        idPortenUser.setId(UUID.randomUUID());
//        userMap.put(idPortenUser.getId().toString(), idPortenUser);
//        return idPortenUser;

        UserEntity userSaved = userRepository.save(copyData(idPortenUser));
        return convert(userSaved);
    }

    public IDPortenUser updateUser(String id, IDPortenUser idPortenUser) {
        Assert.notNull(id, "id is mandatory");
        Assert.isTrue(Objects.equals(id, idPortenUser.getId().toString()), "id must match resource.id");
//        return userMap.replace(id, idPortenUser);
        UserEntity savedUser = userRepository.save(copyData(idPortenUser));
        return convert(savedUser);
    }

    public IDPortenUser deleteUser(String id) {
//        return userMap.remove(id);
        UUID uuid = UUID.fromString(id);
        Optional<UserEntity> userExists = userRepository.findByUuid(UUID.fromString(id));
        if (userExists.isEmpty()) {
            return null;
        }
        userRepository.delete(UserEntity.builder().uuid(uuid).build());

        return convert(userExists.get());
    }

    private IDPortenUser convert(UserEntity u) {
        if (u == null) {
            return null;
        }
        return new IDPortenUser(u);
    }

    private UserEntity copyData(IDPortenUser idPortenUser) {
        return idPortenUser.toEntity();
    }

}
