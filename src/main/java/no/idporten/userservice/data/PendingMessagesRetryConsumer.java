package no.idporten.userservice.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;

@Component
@Slf4j
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class PendingMessagesRetryConsumer extends RetryConsumer {

    public PendingMessagesRetryConsumer(RedisTemplate<String, String> updateEidCache, DirectUserService userService) {
        super(updateEidCache, userService);
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void handlePendingMessages() {
        var streamOperations = updateEidCache.opsForStream();
        PendingMessages pendingMessages = streamOperations.pending(UPDATE_LAST_LOGIN_STREAM, Consumer.from(UPDATE_LAST_LOGIN_GROUP, consumerName));

        if (!pendingMessages.isEmpty()) {
            log.info("{}: Pending messages summary: {}", consumerName, pendingMessages.size());
            if (pingDb()) {
                for (PendingMessage pendingMessage : pendingMessages) {
                    log.info("Attempting to claim or reprocess pending message: {}", pendingMessage.getIdAsString());
                    List<MapRecord<String, Object, Object>> claimedMessages = getClaimedMessages(pendingMessage, streamOperations);

                    if (!claimedMessages.isEmpty()) {
                        for (MapRecord<String, Object, Object> claimedMessage : claimedMessages) {
                            handleMessageAndAcknowledge(claimedMessage);
                        }
                    } else {
                        log.info("Message not available for claim: {}", pendingMessage.getIdAsString());
                    }
                }
            } else {
                log.info("Database is down. Retrying pending messages in a minute");
            }
        }
    }

    private List<MapRecord<String, Object, Object>> getClaimedMessages(PendingMessage pendingMessage, StreamOperations<String, Object, Object> streamOperations) {
        return streamOperations.claim(
                UPDATE_LAST_LOGIN_STREAM,
                UPDATE_LAST_LOGIN_GROUP,
                consumerName,
                Duration.ofSeconds(10L),
                pendingMessage.getId()
        );
    }

}
