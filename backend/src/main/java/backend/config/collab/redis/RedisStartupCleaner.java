package backend.config.collab.redis;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisStartupCleaner {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisStartupCleaner(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @PostConstruct
    public void clearRedisOnStartup() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.flushAll(); // Deletes everything
        }
    }
}
