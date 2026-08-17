package com.vitaledge.service;

import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.product.AgeGroupEnum;
import com.vitaledge.domain.product.Product;
import com.vitaledge.domain.product.ProductCategoryEnum;
import com.vitaledge.domain.product.ProductRule;
import com.vitaledge.domain.product.ProductStatusEnum;
import com.vitaledge.domain.recommendation.Recommendation;
import com.vitaledge.domain.recommendation.RecommendationScoreHistory;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.ProductRepository;
import com.vitaledge.repository.ProductRuleRepository;
import com.vitaledge.repository.RecommendationRepository;
import com.vitaledge.repository.RecommendationScoreHistoryRepository;
import com.vitaledge.util.BmiCalculator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product recommendation engine. Ports the source project's {@code recommendation_service.py}:
 * safe-product filtering against the health profile, deterministic scoring, ranking with
 * confidence values, and persistence of recommendations + per-metric score history.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final int PRODUCT_FETCH_LIMIT = 200;
    private static final int MAX_RECOMMENDATIONS = 5;
    private static final int MAX_RECOMMENDATIONS_COUNSELLOR = 3;

    private final ProductRepository productRepository;
    private final ProductRuleRepository productRuleRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationScoreHistoryRepository scoreHistoryRepository;

    public RecommendationService(ProductRepository productRepository,
                                 ProductRuleRepository productRuleRepository,
                                 RecommendationRepository recommendationRepository,
                                 RecommendationScoreHistoryRepository scoreHistoryRepository) {
        this.productRepository = productRepository;
        this.productRuleRepository = productRuleRepository;
        this.recommendationRepository = recommendationRepository;
        this.scoreHistoryRepository = scoreHistoryRepository;
    }

    public record ScoredProduct(
            Product product,
            double score,
            double confidence,
            boolean safe,
            boolean excluded,
            String reason,
            Map<String, Double> components
    ) {
    }

    @Transactional
    public List<Recommendation> generate(User user, AssessmentSession assessment, HealthProfile healthProfile) {
        List<Product> products = productRepository.findByStatus(ProductStatusEnum.ACTIVE)
                .stream().limit(PRODUCT_FETCH_LIMIT).toList();

        if (products.isEmpty()) {
            log.info("No active products available for recommendation");
            return List.of();
        }

        List<UUID> productIds = products.stream().map(Product::getId).toList();
        Map<UUID, List<ProductRule>> rulesByProduct = productRuleRepository
                .findByProductIdInAndActiveTrue(productIds)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getProduct().getId()));

        Map<String, Object> profile = healthProfile.getProfileData();

        List<ScoredProduct> scored = new ArrayList<>();
        for (Product product : products) {
            Evaluation eval = evaluate(product, rulesByProduct.getOrDefault(product.getId(), List.of()), profile);
            scored.add(new ScoredProduct(
                    product,
                    round(eval.totalScore, 2),
                    round(eval.totalScore / 100.0, 3),
                    eval.safe,
                    eval.excluded,
                    eval.reason,
                    eval.components));
        }

        scored.sort(Comparator
                .comparing((ScoredProduct s) -> s.excluded)
                .thenComparing(Comparator.comparingDouble(ScoredProduct::score).reversed())
                .thenComparing(s -> s.product().getName().toLowerCase()));

        int maxRecommendations = Boolean.TRUE.equals(healthProfile.isCounsellorRecommended())
                ? MAX_RECOMMENDATIONS_COUNSELLOR : MAX_RECOMMENDATIONS;

        List<ScoredProduct> recommended = scored.stream()
                .filter(s -> !s.excluded && s.safe)
                .limit(maxRecommendations)
                .toList();

        recommendationRepository.deleteByAssessmentId(assessment.getId());
        scoreHistoryRepository.deleteByAssessmentId(assessment.getId());

        List<Recommendation> saved = new ArrayList<>();
        int rank = 1;
        for (ScoredProduct sp : recommended) {
            Recommendation reco = new Recommendation(user, assessment, healthProfile, sp.product(),
                    sp.reason(), sp.confidence(), rank++, true, false);
            recommendationRepository.save(reco);
            for (Map.Entry<String, Double> e : sp.components().entrySet()) {
                scoreHistoryRepository.save(new RecommendationScoreHistory(reco, e.getKey(), e.getValue()));
            }
            saved.add(reco);
        }
        return saved;
    }

    private record Evaluation(boolean safe, boolean excluded, double totalScore, String reason,
                              Map<String, Double> components) {
    }

    private Evaluation evaluate(Product product, List<ProductRule> rules, Map<String, Object> profile) {
        Map<String, Double> components = new LinkedHashMap<>();
        StringBuilder reason = new StringBuilder();
        boolean excluded = false;

        // 1. Safety / exclusion rules
        List<String> exclusionReasons = evaluateSafety(product, rules, profile);
        if (!exclusionReasons.isEmpty()) {
            excluded = true;
            components.put("safety", 0.0);
            reason.append("Excluded: ").append(String.join("; ", exclusionReasons)).append(". ");
        } else {
            components.put("safety", 100.0);
        }
        boolean safe = !excluded;

        // 2. Category alignment (from BMI and goals)
        double categoryScore = scoreCategoryMatch(product, profile);
        components.put("category", categoryScore);

        // 3. Age-group alignment
        double ageScore = scoreAgeGroup(product, profile);
        components.put("age_group", ageScore);

        // 4. Dietary alignment
        double dietaryScore = scoreDietaryMatch(product, profile);
        components.put("dietary", dietaryScore);

        // 5. Nutrition/goal alignment
        double nutritionScore = scoreNutritionAlignment(product, profile);
        components.put("nutrition", nutritionScore);

        double total = 50.0 + 0.25 * categoryScore + 0.15 * ageScore + 0.2 * dietaryScore
                + 0.2 * nutritionScore + (safe ? 10.0 : 0.0);
        components.put("overall", round(total, 2));

        if (!excluded) {
            reason.append(buildReason(product, categoryScore, ageScore, dietaryScore, nutritionScore));
        }

        return new Evaluation(safe, excluded, total, reason.toString().trim(), components);
    }

    private List<String> evaluateSafety(Product product, List<ProductRule> rules, Map<String, Object> profile) {
        List<String> reasons = new ArrayList<>();
        Map<String, Object> extracted = safeExtracted(profile);

        // Allergen intersection
        Object allergies = extracted.get(HealthProfileService.KEY_ALLERGIES);
        List<String> userAllergies = toStringList(allergies);
        List<String> productAllergens = product.getAllergens();
        for (String allergen : productAllergens) {
            if (userAllergies.stream().anyMatch(a -> a.equalsIgnoreCase(allergen))) {
                reasons.add("contains allergen: " + allergen);
            }
        }

        // Pregnancy
        if (Boolean.TRUE.equals(boolValue(extracted.get(HealthProfileService.KEY_PREGNANCY)))
                && containsAnyTag(product, List.of("not_for_pregnancy", "pregnancy_safety_warning"))) {
            reasons.add("not recommended during pregnancy");
        }

        for (ProductRule rule : rules) {
            Map<String, Object> ruleDef = rule.getRuleDefinition();
            if (ruleDef == null) {
                continue;
            }
            reasons.addAll(evaluateRule(ruleDef, extracted));
        }
        return reasons;
    }

    private List<String> evaluateRule(Map<String, Object> ruleDef, Map<String, Object> extracted) {
        List<String> reasons = new ArrayList<>();
        Object notFor = ruleDef.get("not_for_age_groups");
        if (notFor instanceof List<?> list) {
            String userAgeGroup = stringValue(extracted.get(HealthProfileService.KEY_AGE_GROUP));
            for (Object o : list) {
                if (o != null && o.toString().equalsIgnoreCase(userAgeGroup)) {
                    reasons.add("not suitable for age group: " + userAgeGroup);
                }
            }
        }
        Object medicalExclusions = ruleDef.get("medical_exclusions");
        if (medicalExclusions instanceof List<?> list) {
            List<String> conditions = toStringList(extracted.get(HealthProfileService.KEY_MEDICAL));
            for (Object o : list) {
                if (o != null && conditions.stream().anyMatch(c -> c.equalsIgnoreCase(o.toString()))) {
                    reasons.add("contraindicated for condition: " + o);
                }
            }
        }
        Object allergens = ruleDef.get("allergens");
        if (allergens instanceof List<?> list) {
            List<String> userAllergies = toStringList(extracted.get(HealthProfileService.KEY_ALLERGIES));
            for (Object o : list) {
                if (o != null && userAllergies.stream().anyMatch(a -> a.equalsIgnoreCase(o.toString()))) {
                    reasons.add("contains allergen: " + o);
                }
            }
        }
        if (Boolean.TRUE.equals(ruleDef.get("exclude_if_pregnant"))
                && Boolean.TRUE.equals(boolValue(extracted.get(HealthProfileService.KEY_PREGNANCY)))) {
            reasons.add("not recommended during pregnancy");
        }
        return reasons;
    }

    private double scoreCategoryMatch(Product product, Map<String, Object> profile) {
        Map<String, Object> extracted = safeExtracted(profile);
        ProductCategoryEnum target = targetCategory(extracted);
        if (product.getCategory() == target) {
            return 100;
        }
        return switch (product.getCategory()) {
            case medium -> 70;
            default -> 40;
        };
    }

    private ProductCategoryEnum targetCategory(Map<String, Object> extracted) {
        Map<String, Object> bmiData = mapValue(profileValue(extracted, "bmi"));
        String classification = stringValue(bmiData.get("classification"));
        if (classification == null) {
            return ProductCategoryEnum.medium;
        }
        return switch (classification) {
            case "underweight" -> ProductCategoryEnum.high;
            case "overweight", "obese_class_i", "obese_class_ii", "obese_class_iii" ->
                    ProductCategoryEnum.low;
            default -> ProductCategoryEnum.medium;
        };
    }

    private double scoreAgeGroup(Product product, Map<String, Object> profile) {
        Map<String, Object> extracted = safeExtracted(profile);
        String userAgeGroup = stringValue(extracted.get(HealthProfileService.KEY_AGE_GROUP));
        if (product.getAgeGroup() == AgeGroupEnum.all) {
            return 80;
        }
        if (userAgeGroup != null && product.getAgeGroup().name().equalsIgnoreCase(userAgeGroup)) {
            return 100;
        }
        return 30;
    }

    private double scoreDietaryMatch(Product product, Map<String, Object> profile) {
        Map<String, Object> extracted = safeExtracted(profile);
        List<String> preferences = toStringList(extracted.get(HealthProfileService.KEY_DIETARY));
        if (preferences.isEmpty()) {
            return 60;
        }
        List<String> tags = product.getDietaryTags();
        long matches = preferences.stream()
                .filter(p -> tags.stream().anyMatch(t -> t.equalsIgnoreCase(p)))
                .count();
        if (matches == 0) {
            return 30;
        }
        return 100.0 * Math.min(1.0, matches / 2.0);
    }

    private double scoreNutritionAlignment(Product product, Map<String, Object> profile) {
        Map<String, Object> extracted = safeExtracted(profile);
        List<String> goals = toStringList(extracted.get(HealthProfileService.KEY_GOALS));
        if (goals.isEmpty()) {
            return 50;
        }
        List<String> tags = product.getDietaryTags();
        long matches = goals.stream()
                .filter(g -> tags.stream().anyMatch(t -> t.equalsIgnoreCase(g)
                        || t.contains(goalTagKey(g))))
                .count();
        if (matches == 0) {
            return 40;
        }
        return 100.0 * Math.min(1.0, matches / 2.0);
    }

    private String goalTagKey(String goal) {
        return switch (goal.toLowerCase()) {
            case "weight_loss" -> "low_calorie";
            case "weight_gain" -> "high_protein";
            case "muscle_gain" -> "high_protein";
            case "general_health" -> "balanced";
            case "digestive_health" -> "probiotic";
            case "immunity" -> "vitamin_c";
            default -> goal.toLowerCase().replace(' ', '_');
        };
    }

    private String buildReason(Product product, double categoryScore, double ageScore,
                               double dietaryScore, double nutritionScore) {
        List<String> parts = new ArrayList<>();
        if (categoryScore >= 100) {
            parts.add("matches your recommended nutrition category");
        }
        if (ageScore >= 100) {
            parts.add("suited to your age group");
        }
        if (dietaryScore >= 100) {
            parts.add("fits your dietary preferences");
        }
        if (nutritionScore >= 100) {
            parts.add("supports your health goals");
        }
        if (parts.isEmpty()) {
            parts.add("generally suitable for your profile");
        }
        return "Recommended because it " + String.join(", ", parts) + ".";
    }

    private static boolean containsAnyTag(Product product, List<String> tags) {
        return product.getDietaryTags().stream().anyMatch(tags::contains);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeExtracted(Map<String, Object> profile) {
        Object extracted = profile.get("extracted");
        if (extracted instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static Object profileValue(Map<String, Object> map, String key) {
        return map.get(key);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
            return out;
        }
        if (value instanceof String s) {
            return s.isBlank() ? List.of() : List.of(s);
        }
        return List.of();
    }

    private static String stringValue(Object value) {
        if (value instanceof String s) {
            return s;
        }
        return value == null ? null : String.valueOf(value);
    }

    private static boolean boolValue(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}