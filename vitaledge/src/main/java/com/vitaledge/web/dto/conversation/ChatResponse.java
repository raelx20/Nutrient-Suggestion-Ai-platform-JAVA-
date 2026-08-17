package com.vitaledge.web.dto.conversation;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        MessageResponse userMessage,
        MessageResponse assistantMessage,
        String sessionType,
        List<Action> suggestedActions
) {
    public record Action(String type, String label, Object payload) {
    }
}