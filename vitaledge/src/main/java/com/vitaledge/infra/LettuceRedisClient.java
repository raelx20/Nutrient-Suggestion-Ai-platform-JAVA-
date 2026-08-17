package com.vitaledge.infra;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Lettuce-backed {@link RedisClient}. The sliding-window limit runs as a single
 * atomic Lua script so concurrent requests cannot race the counter.
 */
public class LettuceRedisClient implements RedisClient {

    private static final Logger log = LoggerFactory.getLogger(LettuceRedisClient.class);

    private static final DefaultRedisScript<Boolean> SLIDING_WINDOW_SCRIPT =
            new DefaultRedisScript<>("""
                    redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1] - ARGV[2])
                    local count = redis.call('ZCARD', KEYS[1])
                    if count >= tonumber(ARGV[3]) then
                        return false
                    end
                    redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4])
                    redis.call('EXPIRE', KEYS[1], ARGV[2])
                    return true
                    """, Boolean.class);

    private final StringRedisTemplate redis;

    public LettuceRedisClient(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    @Override
    public void setex(String key, long ttlSeconds, String value) {
        redis.opsForValue().set(key, value, java.time.Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void delete(String key) {
        redis.delete(key);
    }

    @Override
    public long deletePattern(String pattern) {
        long deleted = 0;
        try (Cursor<String> cursor = redis.scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {
            Set<String> batch = new java.util.LinkedHashSet<>();
            while (cursor.hasNext()) {
                batch.add(cursor.next());
                if (batch.size() >= 100) {
                    deleted += flush(batch);
                }
            }
            deleted += flush(batch);
        } catch (Exception e) {
            log.warn("Redis SCAN/DEL for pattern {} failed: {}", pattern, e.getMessage());
        }
        return deleted;
    }

    private long flush(Set<String> keys) {
        if (keys.isEmpty()) {
            return 0;
        }
        Long removed = redis.delete(keys);
        keys.clear();
        return removed == null ? 0 : removed;
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    @Override
    public boolean ping() {
        try {
            return "PONG".equalsIgnoreCase(redis.getConnectionFactory().getConnection().ping());
        } catch (Exception e) {
            log.warn("Redis ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean slidingWindowAllow(String key, int limit, int windowSeconds) {
        try {
            long now = System.currentTimeMillis();
            String member = java.util.UUID.randomUUID().toString();
            Boolean allowed = redis.execute(SLIDING_WINDOW_SCRIPT, java.util.List.of(key),
                    String.valueOf(now), String.valueOf(windowSeconds * 1000L),
                    String.valueOf(limit), member);
            return Boolean.TRUE.equals(allowed);
        } catch (Exception e) {
            log.warn("Sliding window check for {} failed: {}", key, e.getMessage());
            // Fail open for global/general limits to avoid DoS on Redis outages;
            // security-sensitive limits callers decide by catching and re-checking.
            return true;
        }
    }
}