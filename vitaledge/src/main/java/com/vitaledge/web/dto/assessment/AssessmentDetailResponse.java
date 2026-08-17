package com.vitaledge.web.dto.assessment;

import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.HealthProfile;
import java.util.List;
import java.util.UUID;

public record AssessmentDetailResponse(
        UUID id,
        String status,
        AssessmentSummary assessment,
        HealthProfileResponse healthProfile,
        List<RecommendationResponse> recommendations
) {
    public static AssessmentDetailResponse of(AssessmentSession session,
                                              HealthProfile hp,
                                              List<RecommendationResponse> recommendations) {
        return new AssessmentDetailResponse(
                session.getId(),
                session.getStatus().name(),
                AssessmentSummary.of(session, null, 0),
                hp == null ? null : HealthProfileResponse.from(hp),
                recommendations);
    }
}