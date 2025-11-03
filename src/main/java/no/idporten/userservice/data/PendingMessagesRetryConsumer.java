package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;

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
        PendingMessagesSummary pendingSummary = streamOperations.pending(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP);
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
                    List<MapRecord<String, Object, Object>> claimedMessages = getClaimedMessages(pendingMessage, streamOperations);

                    if (!claimedMessages.isEmpty()) {
                        for (MapRecord<String, Object, Object> claimedMessage : claimedMessages) {
                            handleMessageAndAcknowledge(claimedMessage);
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

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void handleOrphanedPendingMessages() {
        log.info("Checking for orphaned pending messages");
        if (pingDb()) {
            var streamOperations = updateEidCache.opsForStream();
            PendingMessages pendingMessages = streamOperations.pending(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, Range.unbounded(), 500);
            if (pendingMessages.isEmpty()) return;

            Set<String> liveConsumerNames = streamOperations.consumers(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP).stream()
                    .map(StreamInfo.XInfoConsumer::consumerName)
                    .collect(Collectors.toSet());

            List<RecordId> orphanedMessages = filterOrphanedMessages(pendingMessages, liveConsumerNames);

            log.info("A total of {} orphaned messages found", orphanedMessages.size());

            if (!orphanedMessages.isEmpty()) {
                List<MapRecord<String, Object, Object>> claimedMessages =
                        streamOperations.claim(
                                UPDATE_LAST_LOGIN_STREAM,
                                UPDATE_LAST_LOGIN_GROUP,
                                consumerName,
                                Duration.ofSeconds(10),
                                orphanedMessages.toArray(new RecordId[0]));

                for (MapRecord<String, Object, Object> claimMessage : claimedMessages) {
                    handleMessageAndAcknowledge(claimMessage);
                }
            }
        }
    }

    private static List<RecordId> filterOrphanedMessages(PendingMessages pending, Set<String> liveConsumerNames) {
        List<RecordId> orphaned = new ArrayList<>();
        for (PendingMessage pm : pending) {
            String owner = pm.getConsumerName();
            if (!liveConsumerNames.contains(owner)) {
                orphaned.add(pm.getId());
            }
        }
        return orphaned;
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

    private void handleMessageAndAcknowledge(MapRecord<String, Object, Object> updateEidMessage) {
        Map<Object, Object> claimedMessage = updateEidMessage.getValue();
        userService.updateUserWithEid(
                UUID.fromString((String) claimedMessage.get("userId")),
                Login.builder().eidName((String) claimedMessage.get("eidName")).lastLogin(Instant.ofEpochMilli(Long.parseLong((String) claimedMessage.get("loginTimeInEpochMillis")))).build()
        );
        updateEidCache.opsForStream().acknowledge(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, updateEidMessage.getId());
        log.info("Message acknowledged: {}", updateEidMessage.getId());
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
