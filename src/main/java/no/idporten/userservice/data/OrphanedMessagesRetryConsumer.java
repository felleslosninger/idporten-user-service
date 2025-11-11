package no.idporten.userservice.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;

@Component
@Slf4j
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class OrphanedMessagesRetryConsumer extends RetryConsumer {

    public OrphanedMessagesRetryConsumer(RedisTemplate<String, String> updateEidCache, DirectUserService userService) {
        super(updateEidCache, userService);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void handleOrphanedPendingMessages() {
        if (pingDb()) {
            createConsumerGroupIfItDoesNotExist();

            var streamOperations = updateEidCache.opsForStream();
            PendingMessages pendingMessages = streamOperations.pending(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, Range.unbounded(), 500);

            if (pendingMessages != null && !pendingMessages.isEmpty()) {
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
    }

    private static List<RecordId> filterOrphanedMessages(PendingMessages pending, Set<String> liveConsumerNames) {
        List<RecordId> orphaned = new ArrayList<>();
        for (PendingMessage pm : pending) {
            String owner = pm.getConsumerName();
            log.info("Active consumers: {}", liveConsumerNames);        // todo fjernes
            log.info("Owner of orphaned message: {}", owner);           // todo fjernes
            if (!liveConsumerNames.contains(owner)) {
                orphaned.add(pm.getId());
            }
        }
        log.info("A total of {} orphaned filtered", pending.size() - orphaned.size());
        return orphaned;
    }

}
