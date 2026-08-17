package com.vitaledge.service;

import com.vitaledge.infra.RedisClient;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Namespaced cache helper. Ports {@code utils/cache.py} (RedisCache) using the
 * configured namespace prefix so the cache never collides across deployments.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final RedisClient redis;
    private final String namespace;

    public CacheService(RedisClient redis, com.vitaledge.config.ApplicationProperties properties) {
        this.redis = redis;
        this.namespace = properties.getCache().getNamespace();
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redis.get(fullKey(key)));
    }

    public void set(String key, String value, long ttlSeconds) {
        try {
            redis.setex(fullKey(key), ttlSeconds, value);
        } catch (Exception e) {
            log.warn("Cache set failed for {}: {}", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            redis.delete(fullKey(key));
        } catch (Exception e) {
            log.warn("Cache delete failed for {}: {}", key, e.getMessage());
        }
    }

    public void deleteByPattern(String pattern) {
        try {
            redis.deletePattern(fullKey(pattern));
        } catch (Exception e) {
            log.warn("Cache pattern delete failed for {}: {}", pattern, e.getMessage());
        }
    }

    public boolean ping() {
        return redis.ping();
    }

    private String fullKey(String key) {
        return namespace + ":" + key;
    }
}