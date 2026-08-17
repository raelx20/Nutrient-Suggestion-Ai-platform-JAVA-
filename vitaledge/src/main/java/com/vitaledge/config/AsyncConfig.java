package com.vitaledge.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async execution for non-blocking jobs (audit logging, AI orchestration,
 * recommendation generation). Mirrors the source project's Celery workers.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditLogExecutor")
    public Executor auditLogExecutor() {
        return executor(2, 8, "audit-");
    }

    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        return executor(2, 4, "ai-");
    }

    private Executor executor(int core, int max, String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }
}