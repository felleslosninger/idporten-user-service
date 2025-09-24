package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@RequiredArgsConstructor
@Component
public class CacheUpdater {

    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        idportenUserCache.opsForValue().set(userCreatedEvent.idPortenUser.getPid(), userCreatedEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userCreatedEvent.idPortenUser.getId().toString(), userCreatedEvent.idPortenUser.getPid());
    }

    @EventListener
    @Async
    public void handleUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        idportenUserCache.opsForValue().set(userUpdatedEvent.idPortenUser.getPid(), userUpdatedEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userUpdatedEvent.idPortenUser.getId().toString(), userUpdatedEvent.idPortenUser.getPid());
    }

    @EventListener
    @Async
    public void handleUserReadEvent(UserReadEvent userReadEvent) {
        idportenUserCache.opsForValue().set(userReadEvent.idPortenUser.getPid(), userReadEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userReadEvent.idPortenUser.getId().toString(), userReadEvent.idPortenUser.getPid());
    }

    @EventListener
    @Async
    public void handleUserDeletedEvent(UserDeletedEvent userDeletedEvent) {
        idportenUserCache.opsForValue().getAndDelete(userDeletedEvent.personIdentification);
        uuidToUseridCache.opsForValue().getAndDelete(userDeletedEvent.userID.toString());
    }

    @EventListener
    @Async
    public void handleUserPidUpdatedEvent(UserPidUpdatedEvent uuserPidUpdated) {
        idportenUserCache.opsForValue().getAndDelete(uuserPidUpdated.oldPid);
        idportenUserCache.opsForValue().set(uuserPidUpdated.idPortenUser.getPid(), uuserPidUpdated.idPortenUser);
        uuidToUseridCache.opsForValue().set(uuserPidUpdated.idPortenUser.getId().toString(), uuserPidUpdated.idPortenUser.getPid());
    }

}
