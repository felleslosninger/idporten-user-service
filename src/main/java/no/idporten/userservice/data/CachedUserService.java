package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CachedUserService implements UserService {

    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;

    private final DirectUserService userService;

    public IDPortenUser findUser(UUID uuid) {
        String cachedUser = uuidToUseridCache.opsForValue().get(uuid.toString());

        if (cachedUser != null) {
            return searchForUser(cachedUser).orElse(null);
        }

        return userService.findUser(uuid);
    }

    public Optional<IDPortenUser> searchForUser(String personIdentifier) {
        IDPortenUser idPortenUser = idportenUserCache.opsForValue().get(personIdentifier);

        if (idPortenUser == null) {
            log.info("Cached user not found: {}", personIdentifier);
            return userService.searchForUser(personIdentifier);
        }

        return Optional.of(idPortenUser);
    }

    @Transactional
    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        return userService.createUser(idPortenUser);
    }

    @Transactional
    public IDPortenUser createStatusUser(IDPortenUser idPortenUser) {
        return userService.createStatusUser(idPortenUser);
    }

    @Transactional
    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        return userService.updateUser(idPortenUser);
    }

    @Transactional
    public IDPortenUser updateUserWithEid(UUID userUuid, Login eid) {
        IDPortenUser user = findUser(userUuid);
        if (user == null) {
            throw UserServiceException.userNotFound();
        }

        return userService.updateUserWithEid(user.getId(), eid);
    }

    @Transactional
    public IDPortenUser deleteUser(UUID userUuid) {
        return userService.deleteUser(userUuid);
    }

    @Transactional
    public IDPortenUser changePid(String currentPid, String newPid) {
        return userService.changePid(currentPid, newPid);
    }
}
