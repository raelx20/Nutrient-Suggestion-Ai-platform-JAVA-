package com.vitaledge.domain.operations;

import com.vitaledge.domain.BaseEntity;
import com.vitaledge.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created", columnList = "created_at")
        })
@Getter
@Setter
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public AuditLog() {
    }

    public AuditLog(User user, String action, String resourceType, UUID resourceId,
                    Map<String, Object> metadata, String ipAddress, String userAgent) {
        this.user = user;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
