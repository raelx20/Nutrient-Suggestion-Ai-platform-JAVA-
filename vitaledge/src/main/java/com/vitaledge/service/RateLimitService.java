package com.vitaledge.service;

import com.vitaledge.common.exception.RateLimitException;
import com.vitaledge.infra.RedisClient;
import org.springframework.stereotype.Service;

/**
 * Sliding-window rate limiting backed by Redis. Ports the {@code _check_rate_limit}
 * helpers from the source project's auth service and the global request middleware.
 *
 * <p>Security-sensitive limits fail closed: if the backing store is unavailable the
 * request is rejected rather than letting abuse through. General limits fail open.</p>
 */
@Service
public class RateLimitService {

    public static final String GLOBAL_KEY = "global";

    private final RedisClient redis;

    public RateLimitService(RedisClient redis) {
        this.redis = redis;
    }

    public void check(String scope, String identifier, int limit, int windowSeconds) {
        checkWithBehavior(scope, identifier, limit, windowSeconds, true);
    }

    public void checkFailOpen(String scope, String identifier, int limit, int windowSeconds) {
        checkWithBehavior(scope, identifier, limit, windowSeconds, false);
    }

    private void checkWithBehavior(String scope, String identifier, int limit, int windowSeconds,
                                   boolean failClosed) {
        String key = "rl:" + scope + ":" + identifier;
        boolean allowed;
        try {
            allowed = redis.slidingWindowAllow(key, limit, windowSeconds);
        } catch (Exception e) {
            if (failClosed) {
                throw new RateLimitException("Rate limit service unavailable", 60);
            }
            return;
        }
        if (!allowed) {
            throw new RateLimitException(
                    "Rate limit exceeded for " + scope + " (" + limit + " per "
                            + (windowSeconds / 3600.0) + "h). Please try again later.",
                    windowSeconds);
        }
    }

    public String keyForIp(String ip, int trustedProxies) {
        return ip;
    }
}