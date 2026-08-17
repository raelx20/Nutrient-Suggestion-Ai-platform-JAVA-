package com.vitaledge.web.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 30) String categoryCode,
        @NotBlank String category,
        @NotBlank String ageGroup,
        String status,
        @Size(max = 1000) String description,
        List<String> allergens,
        List<String> dietaryTags,
        Map<String, Object> nutritionalInfo
) {
}