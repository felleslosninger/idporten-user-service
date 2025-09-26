package no.idporten.userservice.config;

import no.idporten.userservice.data.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class UserServiceConfig {

    @Bean(name="userService")
    @ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
    public UserService cachedUserService(RedisTemplate<String,
            IDPortenUser> idportenUserCache, RedisTemplate<String,
            String> uuidToUseridCache, RedisTemplate<String, String> updateEidCache,
            DirectUserService userService) {
        return new CachedUserService(idportenUserCache, uuidToUseridCache, updateEidCache, userService);
    }

    @Bean(name="userService")
    @ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "false")
    public UserService directUserService(UserRepository userRepository) {
        return new DirectUserService(userRepository);
    }

}
