package com.vitaledge.domain.user;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class Permission extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Permission() {
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }
}