package no.idporten.userservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class BaseRedisTest {

    private static RedisServer redisServer;

    private static int redisPort;

    @BeforeAll
    static void startRedisServer() throws IOException{
        if (redisServer == null) {
            redisPort = findFreePort();
            redisServer = new RedisServer(redisPort);
            redisServer.start();
        }
    }

    @AfterAll
    static void stopRedisServer() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> String.valueOf(redisPort));
        registry.add("spring.data.redis.host", () -> "localhost");
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}