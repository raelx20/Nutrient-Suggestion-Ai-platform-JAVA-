package com.vitaledge.config;

import com.vitaledge.config.ApplicationProperties.RateLimit;
import com.vitaledge.repository.UserRepository;
import com.vitaledge.service.AuditLogService;
import com.vitaledge.service.RateLimitService;
import com.vitaledge.web.filter.AuditLogFilter;
import com.vitaledge.web.filter.GlobalRateLimitFilter;
import com.vitaledge.web.filter.RequestIdFilter;
import com.vitaledge.web.filter.SecurityHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the servlet filter chain in a deterministic order. All filters run
 * after Spring Security's filter chain so authenticated users are available.
 */
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        return registration(new RequestIdFilter(), 1);
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        return registration(new SecurityHeadersFilter(), 2);
    }

    @Bean
    public FilterRegistrationBean<GlobalRateLimitFilter> globalRateLimitFilter(
            RateLimitService rateLimitService, ApplicationProperties properties) {
        RateLimit rl = properties.getRateLimit();
        return registration(new GlobalRateLimitFilter(rateLimitService,
                rl.getGlobalRequestsPerMinute(), rl.getTrustedProxyCount()), 3);
    }

    @Bean
    public FilterRegistrationBean<AuditLogFilter> auditLogFilter(AuditLogService auditLogService,
                                                                 UserRepository userRepository) {
        return registration(new AuditLogFilter(auditLogService, userRepository), 4);
    }

    private <T extends jakarta.servlet.Filter> FilterRegistrationBean<T> registration(T filter, int order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }
}