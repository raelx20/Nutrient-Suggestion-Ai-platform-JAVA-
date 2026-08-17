package com.vitaledge.web.dto.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String status,
        boolean verified,
        boolean active,
        List<String> roles,
        LocalDateTime createdAt
) {
    public static UserResponse from(com.vitaledge.domain.user.User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus() == null ? null : user.getStatus().name(),
                user.isVerified(),
                user.isActive(),
                user.getRoles().stream().map(r -> r.getName()).sorted().toList(),
                user.getCreatedAt());
    }
}