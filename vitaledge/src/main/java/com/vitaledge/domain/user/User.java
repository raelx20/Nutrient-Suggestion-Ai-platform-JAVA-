package com.vitaledge.domain.user;

import com.vitaledge.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.pending;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(name = "notification_preferences")
    private String notificationPreferences;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    public User() {
    }

    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void markVerified() {
        this.verified = true;
        this.status = UserStatus.verified;
    }

    public void lock() {
        this.locked = true;
        this.active = false;
        this.status = UserStatus.locked;
    }
}