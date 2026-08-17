package com.vitaledge.service;

import com.vitaledge.domain.operations.AuditLog;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.AuditLogRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async, non-blocking audit logging. Ports the source project's {@code AuditLogMiddleware}.
 * Failures never propagate to the caller.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(String action, String resourceType, UUID resourceId,
                       Map<String, Object> metadata, String ipAddress, String userAgent,
                       Optional<User> user) {
        recordAsync(action, resourceType, resourceId, metadata, ipAddress, userAgent, user);
    }

    @Async("auditLogExecutor")
    public void recordAsync(String action, String resourceType, UUID resourceId,
                            Map<String, Object> metadata, String ipAddress, String userAgent,
                            Optional<User> user) {
        try {
            Map<String, Object> safeMetadata = metadata == null ? new LinkedHashMap<>() : metadata;
            repository.save(new AuditLog(user.orElse(null), action, resourceType, resourceId,
                    safeMetadata, ipAddress, truncate(userAgent, 255)));
        } catch (Exception e) {
            log.warn("Audit log write failed for action {}: {}", action, e.getMessage());
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}