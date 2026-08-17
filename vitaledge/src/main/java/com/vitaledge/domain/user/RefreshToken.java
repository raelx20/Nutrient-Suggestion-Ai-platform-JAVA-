package com.vitaledge.domain.user;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "token_hash"),
        indexes = {
                @Index(name = "idx_refresh_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_expires_at", columnList = "expires_at"),
                @Index(name = "idx_refresh_family_id", columnList = "family_id")
        })
@Getter
@Setter
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    public RefreshToken() {
    }

    public RefreshToken(User user, String tokenHash, UUID familyId, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public void revokeAndReplace(UUID replacementId) {
        revoke();
        this.replacedBy = replacementId;
    }
}