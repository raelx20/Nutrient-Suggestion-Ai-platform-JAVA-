package com.vitaledge.web.dto.product;

import com.vitaledge.domain.product.Product;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String sku,
        String category,
        String categoryCode,
        String ageGroup,
        String status,
        String description,
        List<String> allergens,
        List<String> dietaryTags,
        Map<String, Object> nutritionalInfo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getCategory() == null ? null : p.getCategory().name(),
                p.getCategoryCode(),
                p.getAgeGroup() == null ? null : p.getAgeGroup().name(),
                p.getStatus() == null ? null : p.getStatus().name(),
                p.getDescription(),
                p.getAllergens(),
                p.getDietaryTags(),
                p.getNutritionalInfo(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}