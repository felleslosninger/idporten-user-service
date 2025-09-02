package no.idporten.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.idporten.userservice.data.IDPortenUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCache {

    @Bean("idportenUserCache")
    public RedisTemplate<String, IDPortenUser> idportenUserCache(RedisConnectionFactory rcf) {
        RedisTemplate<String, IDPortenUser> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<IDPortenUser> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, IDPortenUser.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonRedisSerializer);

        return template;
    }

    @Bean("uuidToUseridCache")
    public RedisTemplate<String, String> uuidToUseridCache(RedisConnectionFactory rcf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }

}
