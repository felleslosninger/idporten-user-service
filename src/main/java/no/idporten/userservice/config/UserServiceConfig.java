package no.idporten.userservice.config;

import no.idporten.userservice.data.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class UserServiceConfig {

    @Value("${digdir.caching.enabled}")
    private String cachingEnabled;

    @Bean(name="userService")
    @ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "true")
    public UserService cachedUserService(RedisTemplate<String, IDPortenUser> idportenUserCache, RedisTemplate<String, String> uuidToUseridCache, DirectUserService userService) {
        return new CachedUserService(idportenUserCache, uuidToUseridCache, userService);
    }

    @Bean(name="userService")
    @ConditionalOnProperty(name = "digdir.caching.enabled", havingValue = "false")
    public UserService directUserService(UserRepository userRepository) {
        return new DirectUserService(userRepository);
    }

}
