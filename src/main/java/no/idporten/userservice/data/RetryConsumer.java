package no.idporten.userservice.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;

@Slf4j
abstract public class RetryConsumer {

    protected final RedisTemplate<String, String> updateEidCache;
    protected final DirectUserService userService;
    protected final String consumerName = ConsumerNameProvider.getConsumerName();

    public RetryConsumer(RedisTemplate<String, String> updateEidCache, DirectUserService userService) {
        this.updateEidCache = updateEidCache;
        this.userService = userService;
    }

    protected boolean pingDb() {
        try {
            userService.findUser(UUID.randomUUID());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void handleMessageAndAcknowledge(MapRecord<String, Object, Object> updateEidMessage) {
        Map<Object, Object> claimedMessage = updateEidMessage.getValue();
        userService.updateUserWithEid(
                UUID.fromString((String) claimedMessage.get("userId")),
                Login.builder().eidName((String) claimedMessage.get("eidName")).lastLogin(Instant.ofEpochMilli(Long.parseLong((String) claimedMessage.get("loginTimeInEpochMillis")))).build()
        );
        updateEidCache.opsForStream().acknowledge(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, updateEidMessage.getId());
        log.info("Message acknowledged: {}", updateEidMessage.getId());
    }

}

