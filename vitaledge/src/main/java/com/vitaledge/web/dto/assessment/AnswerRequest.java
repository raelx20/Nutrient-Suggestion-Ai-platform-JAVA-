package com.vitaledge.web.dto.assessment;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AnswerRequest(
        @NotNull UUID questionId,
        @NotNull Object value
) {
    public record Batch(
            @NotNull List<AnswerRequest> answers
    ) {
    }
}