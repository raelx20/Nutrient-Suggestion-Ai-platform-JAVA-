package com.vitaledge.config;

import com.vitaledge.infra.InMemoryRedisClient;
import com.vitaledge.infra.RedisClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisClient redisClient() {
        return new InMemoryRedisClient();
    }
}