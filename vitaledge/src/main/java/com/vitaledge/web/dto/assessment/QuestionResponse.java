package com.vitaledge.web.dto.assessment;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        String key,
        String text,
        String type,
        List<String> options,
        boolean required,
        int orderIndex
) {
}