package com.vitaledge.infra;

/**
 * Minimal Redis operations used by the application. Kept behind an interface so that
 * tests can substitute an in-memory implementation without requiring a running Redis.
 */
public interface RedisClient {

    String get(String key);

    void setex(String key, long ttlSeconds, String value);

    void delete(String key);

    /** Delete keys matching the glob pattern using SCAN semantics. Returns count deleted. */
    long deletePattern(String pattern);

    boolean exists(String key);

    boolean ping();

    /**
     * Sliding-window rate limit check (atomic).
     *
     * @return true when the request is allowed, false when the limit is exceeded
     */
    boolean slidingWindowAllow(String key, int limit, int windowSeconds);
}