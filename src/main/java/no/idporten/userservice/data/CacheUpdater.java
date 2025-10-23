package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.event.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class CacheUpdater {

    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;

    @Value("digdir.caching.time_to_live_in_days")
    private long ttl;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        idportenUserCache.opsForValue().set(userCreatedEvent.idPortenUser.getPid(), userCreatedEvent.idPortenUser, ttl, TimeUnit.DAYS);
        uuidToUseridCache.opsForValue().set(userCreatedEvent.idPortenUser.getId().toString(), userCreatedEvent.idPortenUser.getPid(), ttl, TimeUnit.DAYS);
    }

    @EventListener
    @Async
    public void handleUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        idportenUserCache.opsForValue().set(userUpdatedEvent.idPortenUser.getPid(), userUpdatedEvent.idPortenUser, ttl, TimeUnit.DAYS);
        uuidToUseridCache.opsForValue().set(userUpdatedEvent.idPortenUser.getId().toString(), userUpdatedEvent.idPortenUser.getPid(), ttl, TimeUnit.DAYS);
    }

    @EventListener
    @Async
    public void handleUserReadEvent(UserReadEvent userReadEvent) {
        idportenUserCache.opsForValue().set(userReadEvent.idPortenUser.getPid(), userReadEvent.idPortenUser, ttl, TimeUnit.DAYS);
        uuidToUseridCache.opsForValue().set(userReadEvent.idPortenUser.getId().toString(), userReadEvent.idPortenUser.getPid(), ttl, TimeUnit.DAYS);
    }

    @EventListener
    @Async
    public void handleUserDeletedEvent(UserDeletedEvent userDeletedEvent) {
        idportenUserCache.opsForValue().getAndDelete(userDeletedEvent.personIdentification);
        uuidToUseridCache.opsForValue().getAndDelete(userDeletedEvent.userID.toString());
    }

    @EventListener
    @Async
    public void handleUserPidUpdatedEvent(UserPidUpdatedEvent userPidUpdated) {
        idportenUserCache.opsForValue().getAndDelete(userPidUpdated.oldPid);
        idportenUserCache.opsForValue().set(userPidUpdated.idPortenUser.getPid(), userPidUpdated.idPortenUser, ttl, TimeUnit.DAYS);
        uuidToUseridCache.opsForValue().set(userPidUpdated.idPortenUser.getId().toString(), userPidUpdated.idPortenUser.getPid(), ttl, TimeUnit.DAYS);
    }

}
