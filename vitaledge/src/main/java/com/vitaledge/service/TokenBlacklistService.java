package com.vitaledge.service;

import com.vitaledge.infra.RedisClient;
import org.springframework.stereotype.Service;

/**
 * Redis-backed blacklist for revoked JWTs. On logout the access token's hash is stored
 * with a TTL equal to its remaining lifetime so a logged-out token is rejected until expiry.
 */
@Service
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:";

    private final RedisClient redis;

    public TokenBlacklistService(RedisClient redis) {
        this.redis = redis;
    }

    public void blacklist(String tokenHash, long ttlSeconds) {
        redis.setex(PREFIX + tokenHash, ttlSeconds, "1");
    }

    public boolean isBlacklisted(String tokenHash) {
        return redis.exists(PREFIX + tokenHash);
    }
}