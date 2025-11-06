package no.idporten.userservice.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrphanedMessagesRetryConsumerTest {

    @Mock
    private DirectUserService directUserService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    private OrphanedMessagesRetryConsumer consumer;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        consumer = new OrphanedMessagesRetryConsumer(redisTemplate, directUserService);
    }

    @Test
    void handleOrphanedPendingMessages() {
        PendingMessage orphan = new PendingMessage(RecordId.of("1-0"), Consumer.from(UPDATE_LAST_LOGIN_GROUP, "deadconsumersociety"), Duration.ofSeconds(100), 1L);
        List<PendingMessage> pendingMessageList = List.of(orphan);
        PendingMessages pendingMessages = new PendingMessages(UPDATE_LAST_LOGIN_GROUP, pendingMessageList);
        when(streamOps.pending(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, Range.unbounded(), 500L)).thenReturn(pendingMessages);

        when(streamOps.consumers(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP)).thenReturn(mock(StreamInfo.XInfoConsumers.class));

        MapRecord<String, Object, Object> updateEidMessage = mock(MapRecord.class);

        Map<Object, Object> values = new HashMap<>();
        values.put("userId", UUID.randomUUID().toString());
        values.put("eidName", "TestEid");
        values.put("loginTimeInEpochMillis", "1688630496413");

        when(updateEidMessage.getValue()).thenReturn(values);

        when(streamOps.claim(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, consumer.consumerName, Duration.ofSeconds(10), orphan.getId()))
                .thenReturn(List.of(updateEidMessage));

        consumer.handleOrphanedPendingMessages();

        verify(streamOps, times(1))
                .claim(eq(UPDATE_LAST_LOGIN_STREAM), eq(UPDATE_LAST_LOGIN_GROUP), anyString(), eq(Duration.ofSeconds(10)),
                        eq(orphan.getId()));
    }
}
