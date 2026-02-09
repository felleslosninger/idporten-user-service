package no.idporten.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.SmartLifecycle;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisLifecycleConfig implements SmartLifecycle {

    private final RedisServer redisServer;
    private volatile boolean running = false;

    public EmbeddedRedisLifecycleConfig(RedisProperties redisProperties) throws IOException {
        this.redisServer = new RedisServer(redisProperties.getRedisPort());
        start();
    }

    @Override
    public void start() {
        if (!running) {
            try {
                redisServer.start();
                running = true;
                System.out.println("Embedded Redis server started on port: " + redisServer.ports().getFirst());
            } catch (Exception e) {
                System.err.println("Failed to start embedded Redis server: " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if (running) {
            try {
                redisServer.stop();
                running = false;
                System.out.println("Embedded Redis server stopped.");
            } catch (Exception e) {
                System.err.println("Failed to stop embedded Redis server: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}