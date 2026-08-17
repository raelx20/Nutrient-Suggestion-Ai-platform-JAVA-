package com.vitaledge.web.dto.assessment;

import com.vitaledge.domain.assessment.AssessmentSession;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssessmentSummary(
        UUID id,
        String status,
        UUID questionnaireId,
        String questionnaireCode,
        int totalQuestions,
        int answeredQuestions,
        int progressPercent,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static AssessmentSummary of(AssessmentSession session,
                                       java.util.List<QuestionResponse> questions,
                                       int answeredCount) {
        int total = questions == null ? 0 : questions.size();
        AssessmentSummary summary = new AssessmentSummary(
                session.getId(),
                session.getStatus().name(),
                session.getQuestionnaire() == null ? null : session.getQuestionnaire().getId(),
                session.getQuestionnaire() == null ? null : session.getQuestionnaire().getCode(),
                total,
                answeredCount,
                total == 0 ? 0 : (int) Math.round(100.0 * answeredCount / total),
                session.getStartedAt(),
                session.getCompletedAt());
        return summary;
    }
}