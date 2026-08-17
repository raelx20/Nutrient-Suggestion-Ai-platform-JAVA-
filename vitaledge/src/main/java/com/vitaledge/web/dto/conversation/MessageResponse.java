package com.vitaledge.web.dto.conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        String role,
        String content,
        String messageType,
        LocalDateTime createdAt
) {
    public static MessageResponse from(com.vitaledge.domain.conversation.Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getMessageType().name(),
                message.getCreatedAt());
    }
}