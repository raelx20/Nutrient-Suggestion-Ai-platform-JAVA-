package com.vitaledge.web.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank @Size(max = 4000) String message,
        @Size(max = 50) String sessionType
) {
}