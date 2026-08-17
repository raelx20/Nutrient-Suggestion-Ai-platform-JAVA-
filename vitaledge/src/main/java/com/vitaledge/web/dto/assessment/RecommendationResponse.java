package com.vitaledge.web.dto.assessment;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSku,
        String productCategory,
        String productCategoryCode,
        String productAgeGroup,
        String reason,
        double confidence,
        int rank,
        boolean safe,
        boolean excluded,
        List<ScoreComponent> scoreComponents
) {
    public record ScoreComponent(String metric, double value) {
    }
}