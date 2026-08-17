package com.vitaledge.web.dto.assessment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AnswerResponse(
        UUID questionId,
        String questionKey,
        Object value
) {
    public static AnswerResponse from(com.vitaledge.domain.assessment.AssessmentAnswer answer) {
        return new AnswerResponse(
                answer.getQuestion().getId(),
                answer.getQuestion().getKey(),
                answer.getAnswerValue());
    }

    public record BatchResult(
            List<AnswerResponse> answers,
            AssessmentSummary assessment
    ) {
    }
}