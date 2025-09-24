package no.idporten.userservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RedisProperties {
    private int redisPort;
    private String redisHost;

    public RedisProperties(
      @Value("${spring.data.redis.port}") int redisPort,
      @Value("${spring.data.redis.host}") String redisHost) {
        this.redisPort = redisPort;
        this.redisHost = redisHost;
    }
}