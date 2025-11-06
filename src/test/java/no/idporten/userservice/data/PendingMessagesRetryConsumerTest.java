package no.idporten.userservice.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PendingMessagesRetryConsumerTest {

    @Mock
    private DirectUserService directUserService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    private PendingMessagesRetryConsumer consumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        consumer = new PendingMessagesRetryConsumer(redisTemplate, directUserService);
    }

    @Test
    void testHandlePendingMessages_claimAndAcknowledge() {
        PendingMessagesSummary pendingMessagesSummary = mock(PendingMessagesSummary.class);
        when(pendingMessagesSummary.getTotalPendingMessages()).thenReturn(1L);
        when(streamOps.pending(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP)).thenReturn(pendingMessagesSummary);

        PendingMessage pendingMessage = mock(PendingMessage.class);
        when(pendingMessage.getId()).thenReturn(RecordId.of("1-0"));
        when(pendingMessage.getIdAsString()).thenReturn("1-0");
        PendingMessages pendingMessages = mock(PendingMessages.class);
        when(pendingMessages.iterator()).thenReturn(Collections.singletonList(pendingMessage).iterator());
        when(streamOps.pending(
                eq(UPDATE_LAST_LOGIN_STREAM),
                any(Consumer.class),
                eq(Range.unbounded()),
                eq(1L)
        )).thenReturn(pendingMessages);

        // Mock claimed message
        Map<Object, Object> messageBody = Map.of(
                "userId", UUID.randomUUID().toString(),
                "eidName", "testEid",
                "loginTimeInEpochMillis", String.valueOf(System.currentTimeMillis())
        );
        MapRecord<String, Object, Object> claimedRecord = mock(MapRecord.class);
        when(claimedRecord.getId()).thenReturn(RecordId.of("1-0"));
        when(claimedRecord.getValue()).thenReturn(messageBody);
        when(streamOps.claim(
                eq(UPDATE_LAST_LOGIN_STREAM),
                eq(UPDATE_LAST_LOGIN_GROUP),
                anyString(),
                eq(Duration.ofSeconds(10L)),
                eq(RecordId.of("1-0"))
        )).thenReturn(Collections.singletonList(claimedRecord));

        consumer.handlePendingMessages();

        verify(streamOps).acknowledge(UPDATE_LAST_LOGIN_STREAM, UPDATE_LAST_LOGIN_GROUP, RecordId.of("1-0"));
        verify(directUserService).updateUserWithEid(any(UUID.class), any());
    }
}
