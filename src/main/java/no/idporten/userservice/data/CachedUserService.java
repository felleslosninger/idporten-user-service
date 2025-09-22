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

        IDPortenUser user = userService.findUser(uuid);
        if (user == null) {
            return null;
        }

        idportenUserCache.opsForValue().set(user.getPid(), user);

        return user;
    }

    public Optional<IDPortenUser> searchForUser(String personIdentifier) {
        IDPortenUser idPortenUser = idportenUserCache.opsForValue().get(personIdentifier);

        if (idPortenUser == null) {
            log.info("Cached user not found: {}", personIdentifier);
            Optional<IDPortenUser> user = userService.searchForUser(personIdentifier);

            if (user.isPresent()) {
                idPortenUser = user.get();
                idportenUserCache.opsForValue().set(idPortenUser.getPid(), idPortenUser);
                uuidToUseridCache.opsForValue().set(idPortenUser.getId().toString(), idPortenUser.getPid());
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(idPortenUser);
    }

    @Transactional
    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        IDPortenUser savedUser = userService.createUser(idPortenUser);

        idportenUserCache.opsForValue().set(savedUser.getPid(), savedUser);
        uuidToUseridCache.opsForValue().set(savedUser.getId().toString(), savedUser.getPid());

        return savedUser;
    }

    @Transactional
    public IDPortenUser createStatusUser(IDPortenUser idPortenUser) {
        IDPortenUser savedUser = userService.createStatusUser(idPortenUser);
        idportenUserCache.opsForValue().set(savedUser.getPid(), savedUser);

        return savedUser;
    }

    @Transactional
    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        IDPortenUser updatedUser = userService.updateUser(idPortenUser);
        idportenUserCache.opsForValue().set(updatedUser.getPid(), updatedUser);

        return updatedUser;
    }

    @Transactional
    public IDPortenUser updateUserWithEid(UUID userUuid, Login eid) {
        IDPortenUser user = findUser(userUuid);
        if (user == null) {
            throw UserServiceException.userNotFound();
        }

        IDPortenUser updatedUser = userService.updateUserWithEid(user.getId(), eid);
        idportenUserCache.opsForValue().set(updatedUser.getPid(), updatedUser);

        return updatedUser;
    }

    @Transactional
    public IDPortenUser deleteUser(UUID userUuid) {
        IDPortenUser user = userService.findUser(userUuid);
        if (user == null) {
            return null;
        }

        IDPortenUser deletedUser = userService.deleteUser(userUuid);

        idportenUserCache.opsForValue().getAndDelete(user.getPid());
        uuidToUseridCache.opsForValue().getAndDelete(userUuid.toString());

        return deletedUser;
    }

    @Transactional
    public IDPortenUser changePid(String currentPid, String newPid) {
        IDPortenUser idPortenUser = userService.changePid(currentPid, newPid);

        idportenUserCache.opsForValue().getAndDelete(currentPid);
        idportenUserCache.opsForValue().set(idPortenUser.getPid(), idPortenUser);
        uuidToUseridCache.opsForValue().set(idPortenUser.getId().toString(), idPortenUser.getPid());

        return idPortenUser;
    }
}
