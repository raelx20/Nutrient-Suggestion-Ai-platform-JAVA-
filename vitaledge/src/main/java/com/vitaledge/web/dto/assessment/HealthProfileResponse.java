package com.vitaledge.web.dto.assessment;

import com.vitaledge.domain.assessment.HealthProfile;
import java.util.List;
import java.util.Map;

public record HealthProfileResponse(
        double bmi,
        String bmiClassification,
        Map<String, Object> scores,
        List<String> riskFactors,
        boolean counsellorRecommended,
        Map<String, Object> profileData,
        int version
) {
    @SuppressWarnings("unchecked")
    public static HealthProfileResponse from(HealthProfile hp) {
        Map<String, Object> data = hp.getProfileData();
        Map<String, Object> bmiData = new java.util.LinkedHashMap<>();
        Object bmiRaw = data.get("bmi");
        if (bmiRaw instanceof Map<?, ?> m) {
            bmiData = (Map<String, Object>) m;
        }
        double bmi = bmiData.get("value") instanceof Number n ? n.doubleValue() : 0.0;
        Object scoresRaw = data.get("scores");
        Map<String, Object> scores = new java.util.LinkedHashMap<>();
        if (scoresRaw instanceof Map<?, ?> m) {
            scores = (Map<String, Object>) m;
        }
        List<String> riskFactors = new java.util.ArrayList<>();
        Object risksRaw = data.get("risk_factors");
        if (risksRaw instanceof List<?> l) {
            l.forEach(o -> riskFactors.add(String.valueOf(o)));
        }
        return new HealthProfileResponse(
                bmi,
                String.valueOf(bmiData.get("classification")),
                scores,
                riskFactors,
                hp.isCounsellorRecommended(),
                data,
                hp.getVersion());
    }
}