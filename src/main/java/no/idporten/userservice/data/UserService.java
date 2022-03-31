package no.idporten.userservice.data;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {

    private Map<String, IDPortenUser> userMap = new HashMap<>();


    public IDPortenUser findUser(String id) {
        return userMap.get(id);
    }

    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        Assert.isNull(idPortenUser.getId(), "id is assigned by server");
        idPortenUser.setId(UUID.randomUUID());
        userMap.put(idPortenUser.getId().toString(), idPortenUser);
        return idPortenUser;
    }

    public IDPortenUser updateUser(String id, IDPortenUser idPortenUser) {
        Assert.notNull(id, "id is mandatory");
        Assert.isTrue(Objects.equals(id, idPortenUser.getId()), "id must match resource.id");
        return userMap.replace(id, idPortenUser);
    }

    public IDPortenUser deleteUser(String id) {
        return userMap.remove(id);
    }

}
