package no.idporten.userservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;

public abstract class BaseRedisTest {

    private static RedisServer redisServer;

    @BeforeAll
    static void startRedisServer() throws IOException{
        if (redisServer == null) {
            redisServer = new RedisServer();
            redisServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (redisServer != null) {
                    try {
                        redisServer.stop();
                    } catch (Exception ignored) {
                    }
                }
            }));
        }
    }

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.host", () -> "localhost");
    }

}