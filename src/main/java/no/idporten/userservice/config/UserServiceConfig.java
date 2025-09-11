package no.idporten.userservice.config;

import no.idporten.userservice.data.CachedUserService;
import no.idporten.userservice.data.DirectUserService;
import no.idporten.userservice.data.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceConfig {

    @Value("${digdir.caching.enabled}")
    private String cachingEnabled;

    @Bean
    public UserService userService(CachedUserService cachedUserService, DirectUserService directUserService) {
        if ("true".equalsIgnoreCase(cachingEnabled)) {
            return cachedUserService;
        }
        return directUserService;
    }
}
