package com.vitaledge.repository;

import com.vitaledge.domain.operations.AuditLog;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}