package no.idporten.userservice.config;

import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.message.UpdateEidMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

import static no.idporten.userservice.config.RedisStreamConstants.EID_UPDATER;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_EID_STREAM;
import static no.idporten.userservice.config.RedisStreamConstants.EID_GROUP;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class SubscriptionConfig {

    private Subscription subscription;

    @Bean
    public Subscription subscription(RedisConnectionFactory connectionFactory, StreamListener<String, ObjectRecord<String, UpdateEidMessage>> streamListener, RedisTemplate<String, String> updateEidCache) {
        try {
            updateEidCache.afterPropertiesSet();
            updateEidCache.opsForStream().createGroup(UPDATE_EID_STREAM, EID_GROUP);
        } catch (RedisSystemException e) {
            log.info("STREAM - Redis group already exists, skipping Redis group creation: {}", "EID-GROUP");
        }

        var streamOffset = StreamOffset.create(
                UPDATE_EID_STREAM, ReadOffset.lastConsumed()
        );

        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofMillis(100))
                .targetType(UpdateEidMessage.class)
                .batchSize(1)
                .build();

        var container = StreamMessageListenerContainer
                .create(connectionFactory, options);

        var streamReadRequest = StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset)
                .consumer(Consumer.from(EID_GROUP, EID_UPDATER))
                .cancelOnError(t -> false) // skip errors
                .autoAcknowledge(false)
                .build();

       subscription = container.register(streamReadRequest, streamListener);

        container.start();
        return subscription;
    }
}
