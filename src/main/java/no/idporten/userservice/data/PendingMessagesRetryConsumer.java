package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static no.idporten.userservice.config.RedisStreamConstants.*;


@Component
@AllArgsConstructor
@Slf4j
public class PendingMessagesRetryConsumer {

    private final RedisTemplate<String, String> updateEidCache;
    private final DirectUserService userService;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void handlePendingMessages() {
        PendingMessages pendingMessages = updateEidCache.opsForStream().pending(UPDATE_EID_STREAM, Consumer.from(EID_GROUP, UPDATE_EID_STREAM));

        List<RecordId> messageIdToProcess = pendingMessages.stream()
                .map(PendingMessage::getId)
                .toList();

        log.info("Number of pending messages to process: {}", messageIdToProcess.size());

        List<MapRecord<String, Object, Object>> messagesToProcess =
                updateEidCache.opsForStream().claim(UPDATE_EID_STREAM, EID_GROUP, EID_RETRY_UPDATER, Duration.of(10, ChronoUnit.SECONDS), messageIdToProcess.toArray(new RecordId[0]));

        for (MapRecord<String, Object, Object> message : messagesToProcess) {
            log.info("Pending message: {}", message.getValue());
        }
    }

}
