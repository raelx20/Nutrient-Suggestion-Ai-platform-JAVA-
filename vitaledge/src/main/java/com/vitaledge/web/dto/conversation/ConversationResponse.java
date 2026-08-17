package com.vitaledge.web.dto.conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String status,
        String sessionType,
        LocalDateTime startedAt,
        LocalDateTime updatedAt,
        List<MessageResponse> messages
) {
}