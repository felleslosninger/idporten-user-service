package no.idporten.userservice.data;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    IDPortenUser findUser(UUID uuid);
    Optional<IDPortenUser> searchForUser(String personIdentifier);
    IDPortenUser createUser(IDPortenUser idPortenUser);
    IDPortenUser createStatusUser(IDPortenUser idPortenUser);
    IDPortenUser updateUser(IDPortenUser idPortenUser);
    IDPortenUser updateUserWithEid(UUID userUuid, Login eid);
    IDPortenUser deleteUser(UUID userUuid);
    IDPortenUser changePid(String currentPid, String newPid);
}
