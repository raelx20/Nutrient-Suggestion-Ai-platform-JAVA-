package com.vitaledge.web.filter;

import com.vitaledge.common.exception.RateLimitException;
import com.vitaledge.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Global per-IP rate limiting. Ports the source project's global request middleware.
 * Fails open: an unavailable rate-limit store must not take down the whole API.
 */
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final int requestsPerMinute;
    private final int trustedProxies;

    public GlobalRateLimitFilter(RateLimitService rateLimitService, int requestsPerMinute, int trustedProxies) {
        this.rateLimitService = rateLimitService;
        this.requestsPerMinute = requestsPerMinute;
        this.trustedProxies = trustedProxies;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = clientIp(request);
        try {
            rateLimitService.checkFailOpen(RateLimitService.GLOBAL_KEY, ip, requestsPerMinute, 60);
        } catch (RateLimitException ex) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
            response.getWriter().write(
                    "{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\""
                            + ex.getMessage().replace("\"", "'")
                            + "\",\"details\":[]}}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        // Derive client IP honoring trusted proxy count. When untrusted, ignore
        // X-Forwarded-For so clients cannot spoof past the rate limiter.
        String forwarded = request.getHeader("X-Forwarded-For");
        String remote = request.getRemoteAddr();
        if (forwarded != null && trustedProxies > 0) {
            String[] hops = forwarded.split(",");
            if (trustedProxies <= hops.length) {
                return hops[hops.length - trustedProxies].trim();
            }
        }
        return remote;
    }
}