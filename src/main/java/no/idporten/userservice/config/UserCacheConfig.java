package no.idporten.userservice.config;

import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.IDPortenUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
public class UserCacheConfig {

    @Bean("idportenUserCache")
    public RedisTemplate<String, IDPortenUser> idportenUserCache(RedisConnectionFactory rcf) {
        RedisTemplate<String, IDPortenUser> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        JacksonJsonRedisSerializer<IDPortenUser> jsonRedisSerializer = new JacksonJsonRedisSerializer<>(IDPortenUser.class);

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
