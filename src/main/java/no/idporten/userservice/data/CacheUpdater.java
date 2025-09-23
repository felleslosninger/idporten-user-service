package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.event.UserDeletedEvent;
import no.idporten.userservice.data.event.UserPidUpdatedEvent;
import no.idporten.userservice.data.event.UserUpdatedEvent;
import no.idporten.userservice.data.event.UserReadEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CacheUpdater {

    private final RedisTemplate<String, IDPortenUser> idportenUserCache;
    private final RedisTemplate<String, String> uuidToUseridCache;

    @EventListener
    @Async
    public void handleUserCreatedEvent(UserUpdatedEvent userCreatedEvent) {
        idportenUserCache.opsForValue().set(userCreatedEvent.idPortenUser.getPid(), userCreatedEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userCreatedEvent.idPortenUser.getId().toString(), userCreatedEvent.idPortenUser.getPid());
    }

    @EventListener
    @Async
    public void handleUserReadEvent(UserReadEvent userCreatedEvent) {
        idportenUserCache.opsForValue().set(userCreatedEvent.idPortenUser.getPid(), userCreatedEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userCreatedEvent.idPortenUser.getId().toString(), userCreatedEvent.idPortenUser.getPid());
    }

    @EventListener
    @Async
    public void handleUserDeletedEvent(UserDeletedEvent userDeletedEvent) {
        idportenUserCache.opsForValue().getAndDelete(userDeletedEvent.personIdentification);
        uuidToUseridCache.opsForValue().getAndDelete(userDeletedEvent.userID.toString());
    }

    @EventListener
    @Async
    public void handleUserPidUpdatedEvent(UserPidUpdatedEvent userDeletedEvent) {
        idportenUserCache.opsForValue().getAndDelete(userDeletedEvent.oldPid);
        idportenUserCache.opsForValue().set(userDeletedEvent.idPortenUser.getPid(), userDeletedEvent.idPortenUser);
        uuidToUseridCache.opsForValue().set(userDeletedEvent.idPortenUser.getId().toString(), userDeletedEvent.idPortenUser.getPid());
    }

}
