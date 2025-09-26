package no.idporten.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.dbevents.UpdateEidEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class UserCacheConfig {

    private Subscription subscription;

    @Bean
    public Subscription subscription(RedisConnectionFactory connectionFactory, StreamListener<String, ObjectRecord<String, UpdateEidEvent>> streamListener) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, UpdateEidEvent>> options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofMillis(100))
                .targetType(UpdateEidEvent.class)
                .batchSize(1)
                .build();

        StreamMessageListenerContainer<String, ObjectRecord<String, UpdateEidEvent>> container = StreamMessageListenerContainer
                .create(connectionFactory, options);

        subscription = container.receive(
                StreamOffset.fromStart("update-eid"),
                streamListener
        );

        container.start();
        return subscription;
    }

    @Bean("idportenUserCache")
    public RedisTemplate<String, IDPortenUser> idportenUserCache(RedisConnectionFactory rcf) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        RedisTemplate<String, IDPortenUser> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        Jackson2JsonRedisSerializer<IDPortenUser> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, IDPortenUser.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonRedisSerializer);

        return template;
    }

    @Bean("uuidToUseridCache")
    public RedisTemplate<String, String> uuidToUseridCache(RedisConnectionFactory rcf) {
        return getStringStringRedisTemplate(rcf);
    }

    @Bean("updateEidCache")
    public RedisTemplate<String, String> updateEidCache(RedisConnectionFactory rcf) {
        return getStringStringRedisTemplate(rcf);
    }

    private RedisTemplate<String, String> getStringStringRedisTemplate(RedisConnectionFactory rcf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }

}
