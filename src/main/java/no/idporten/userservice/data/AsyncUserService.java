package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import no.idporten.userservice.data.dbevents.UpdateEidEvent;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AsyncUserService implements StreamListener<String, ObjectRecord<String, UpdateEidEvent>> {

    private final DirectUserService userService;
    private final RedisTemplate<String, String> updateEidCache;

    @Override
    public void onMessage(ObjectRecord<String, UpdateEidEvent> updateEidEvent) {
        UpdateEidEvent event = updateEidEvent.getValue();
        userService.updateUserWithEid(event.getUserId(), Login.builder().eidName(event.getEidName()).lastLogin(event.getLoginTime()).build());

        updateEidCache.opsForStream().delete(updateEidEvent);
    }
}
