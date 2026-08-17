package com.vitaledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Common identity/timestamp columns for all entities.
 * UUIDs are generated in the application (like the source project's {@code uuid4}),
 * which keeps the schema portable between PostgreSQL and H2.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @org.hibernate.annotations.UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}