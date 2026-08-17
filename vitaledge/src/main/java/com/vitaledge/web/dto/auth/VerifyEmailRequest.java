package com.vitaledge.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank String token
) {
}