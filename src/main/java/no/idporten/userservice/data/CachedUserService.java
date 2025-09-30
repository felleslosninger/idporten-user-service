package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.message.UpdateEidMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_EID_STREAM;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class CachedUserService implements UserService {

    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;
    private final RedisTemplate<String, String> updateEidCache;

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
        } else {
            log.info("Cached user found: {}", personIdentifier);
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

    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        return userService.updateUser(idPortenUser);
    }

    public IDPortenUser updateUserWithEid(UUID userUuid, Login eid) {
        IDPortenUser user = findUser(userUuid);
        if (user == null) {
            throw UserServiceException.userNotFound();
        }

        UpdateEidMessage updateEidEvent = new UpdateEidMessage(userUuid, Instant.now().toEpochMilli(), eid.getEidName());

        ObjectRecord<String, UpdateEidMessage> eventRecord = StreamRecords.newRecord()
                .ofObject(updateEidEvent)
                .withStreamKey(UPDATE_EID_STREAM);

        updateEidCache.opsForStream().add(eventRecord);

        return IDPortenUser.builder()
                .pid(user.getPid())
                .id(user.getId())
                .closedCode(user.getClosedCode())
                .login(buildIDPortenUser(eid, user))
                .active(user.isActive())
                .build();
    }

    @Transactional
    public IDPortenUser deleteUser(UUID userUuid) {
        return userService.deleteUser(userUuid);
    }

    public IDPortenUser changePid(String currentPid, String newPid) {
        return userService.changePid(currentPid, newPid);
    }

    private Optional<Login> findExistingEid(Login eid, List<Login> existingeIDs) {
        return existingeIDs.stream()
                .filter(e -> e.getEidName().equalsIgnoreCase(eid.getEidName()))
                .findFirst();
    }

    private Login buildIDPortenUser(Login eid, IDPortenUser user) {
        Optional<Login> existingEid = findExistingEid(eid, user.getLogins());
        Login newLogin;

        if (existingEid.isPresent()) {
            newLogin = existingEid.get();
            newLogin.setLastLogin(Instant.now());
        } else {
            newLogin = Login.builder()
                    .eidName(eid.getEidName())
                    .lastLogin(Instant.now())
                    .firstLogin(Instant.now())
                    .build();
        }
        return newLogin;
    }

}
