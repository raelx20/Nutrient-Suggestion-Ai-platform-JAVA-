package com.vitaledge.web.filter;

import com.vitaledge.domain.user.User;
import com.vitaledge.repository.UserRepository;
import com.vitaledge.security.UserPrincipal;
import com.vitaledge.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs an audit entry per request. Runs after Spring Security so the authenticated
 * user (when present) can be associated with the entry.
 */
public class AuditLogFilter extends OncePerRequestFilter {

    private static final int MAX_URI = 255;

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public AuditLogFilter(AuditLogService auditLogService, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/health") || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("method", request.getMethod());
            metadata.put("path", truncate(request.getRequestURI(), MAX_URI));
            metadata.put("status", response.getStatus());
            metadata.put("duration_ms", durationMs);
            metadata.put("request_id", request.getAttribute(RequestIdFilter.HEADER_NAME));

            auditLogService.record(
                    "http_request",
                    null,
                    null,
                    metadata,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    currentUser());
        }
    }

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            try {
                return userRepository.findById(principal.userId());
            } catch (Exception ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}