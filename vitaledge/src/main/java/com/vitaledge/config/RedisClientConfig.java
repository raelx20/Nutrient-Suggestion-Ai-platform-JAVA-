package com.vitaledge.config;

import com.vitaledge.infra.LettuceRedisClient;
import com.vitaledge.infra.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Registers the production {@link RedisClient}. In tests, Redis auto-configuration is
 * excluded and an in-memory {@link RedisClient} is provided instead, so this bean is
 * simply not created.
 */
@Configuration
public class RedisClientConfig {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisClient redisClient(StringRedisTemplate redisTemplate) {
        return new LettuceRedisClient(redisTemplate);
    }
}