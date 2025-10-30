package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static no.idporten.userservice.config.RedisStreamConstants.*;

@Component
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class PendingMessagesRetryConsumer {

    private final RedisTemplate<String, String> updateEidCache;
    private final DirectUserService userService;
    private final String consumerName = ConsumerNameProvider.getConsumerName();

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void handlePendingMessages() {
        var streamOperations = updateEidCache.opsForStream();
        PendingMessagesSummary pendingSummary = streamOperations.pending(UPDATE_LAST_LOGIN_STREAM, ConsumerNameProvider.getConsumerName());
        log.info("Pending messages summary: {}", pendingSummary != null ? pendingSummary.getTotalPendingMessages() : 0);

        if (pendingSummary != null && pendingSummary.getTotalPendingMessages() > 0) {
            if (pingDb()) {
                PendingMessages pendingMessages = streamOperations.pending(
                        UPDATE_LAST_LOGIN_STREAM,
                        Consumer.from(UPDATE_LAST_LOGIN_GROUP, consumerName),
                        Range.unbounded(),
                        pendingSummary.getTotalPendingMessages()
                );

                for (PendingMessage pendingMessage : pendingMessages) {
                    log.info("Attempting to claim or reprocess pending message: {}", pendingMessage.getIdAsString());

                    List<MapRecord<String, Object, Object>> claimedMessages = streamOperations.claim(
                            UPDATE_LAST_LOGIN_STREAM,
                            UPDATE_LAST_LOGIN_GROUP,
                            consumerName,
                            Duration.ofSeconds(10L),
                            pendingMessage.getId()
                    );

                    if (!claimedMessages.isEmpty()) {
                        log.info("Message claimed: {}", pendingMessage.getIdAsString());
                        for (MapRecord<String, Object, Object> claimedMessage : claimedMessages) {
                            log.info("Processing claimed message: {}", claimedMessage.getValue());
                            handleMessage(claimedMessage.getValue());
                            streamOperations.acknowledge(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, claimedMessage.getId());
                            log.info("Message acknowledged: {}", claimedMessage.getId());
                        }
                    } else {
                        log.info("Failed to claim message: {}", pendingMessage.getIdAsString());
                    }
                }
            } else {
                log.info("Database is down. Retrying pending messages in a minute");
            }
        }
    }

    private void handleMessage(Map<Object, Object> updateEidMessage) {
        userService.updateUserWithEid(
                UUID.fromString((String) updateEidMessage.get("userId")),
                Login.builder().eidName((String) updateEidMessage.get("eidName")).lastLogin(Instant.ofEpochMilli(Long.parseLong((String)updateEidMessage.get("loginTimeInEpochMillis")))).build()
        );
    }

    private boolean pingDb() {
        try {
            userService.findUser(UUID.randomUUID());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
