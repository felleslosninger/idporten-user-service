package no.idporten.userservice.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_GROUP;
import static no.idporten.userservice.config.RedisStreamConstants.UPDATE_LAST_LOGIN_STREAM;

@Component
@Slf4j
@DependsOn("redisConnectionFactory")
public class StreamConsumerManager implements SmartLifecycle {

    private final RedisTemplate<String, String> updateEidCache;
    private volatile boolean running = false;

    public StreamConsumerManager(RedisTemplate<String, String> updateEidCache) {
        this.updateEidCache = updateEidCache;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        log.info("Stopping StreamConsumerManager and removing consumer {} from Redis stream group", ConsumerNameProvider.getConsumerName());
        updateEidCache.opsForStream().deleteConsumer(UPDATE_LAST_LOGIN_STREAM, Consumer.from(UPDATE_LAST_LOGIN_GROUP, ConsumerNameProvider.getConsumerName()));
        log.info("Stopped StreamConsumerManager and removing consumer {} from Redis stream group", ConsumerNameProvider.getConsumerName());
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }
}