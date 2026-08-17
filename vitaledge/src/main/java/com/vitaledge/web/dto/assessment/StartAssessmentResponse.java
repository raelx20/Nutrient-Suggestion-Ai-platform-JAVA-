package com.vitaledge.web.dto.assessment;

import com.vitaledge.domain.assessment.AssessmentSession;
import java.util.List;

public record StartAssessmentResponse(
        AssessmentSummary assessment,
        List<QuestionResponse> questions
) {
    public static StartAssessmentResponse of(AssessmentSession session,
                                             List<QuestionResponse> questions,
                                             int answeredCount) {
        AssessmentSummary summary = new AssessmentSummary(
                session.getId(),
                session.getStatus().name(),
                session.getQuestionnaire() == null ? null : session.getQuestionnaire().getId(),
                session.getQuestionnaire() == null ? null : session.getQuestionnaire().getCode(),
                questions.size(),
                answeredCount,
                questions.isEmpty() ? 0 : (int) Math.round(100.0 * answeredCount / questions.size()),
                session.getStartedAt(),
                session.getCompletedAt());
        return new StartAssessmentResponse(summary, questions);
    }
}