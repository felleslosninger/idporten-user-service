package no.idporten.userservice.data;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.message.UpdateEidMessage;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;


@Service
@Slf4j
public class UpdateEidLoginMessagesConsumer extends RetryConsumer implements StreamListener<String, ObjectRecord<String, UpdateEidMessage>> {

    private final DirectUserService userService;
    private final RedisTemplate<String, String> updateEidCache;

    public UpdateEidLoginMessagesConsumer(RedisTemplate<String, String> updateEidCache, DirectUserService userService) {
        super(updateEidCache, userService);
        this.userService = userService;
        this.updateEidCache = updateEidCache;
    }

    @Override
    public void onMessage(ObjectRecord<String, UpdateEidMessage> updateEidEvent) {
        createConsumerGroupIfItDoesNotExist();

        UpdateEidMessage event = updateEidEvent.getValue();

        userService.updateUserWithEid(event.userId(), Login.builder().eidName(event.eidName()).lastLogin(Instant.ofEpochMilli(event.loginTimeInEpochMillis())).build());
        updateEidCache.opsForStream().acknowledge(UPDATE_LAST_LOGIN_GROUP, updateEidEvent);
        log.info("User {} has been updated by message {}", event.userId(), updateEidEvent.getId());

        updateEidCache.opsForStream().delete(updateEidEvent);
    }

}
