package com.vitaledge.web.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresInSeconds,
        UserResponse user
) {
}