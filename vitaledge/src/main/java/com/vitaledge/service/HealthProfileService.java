package com.vitaledge.service;

import com.vitaledge.domain.assessment.AssessmentAnswer;
import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.HealthProfileRepository;
import com.vitaledge.util.BmiCalculator;
import com.vitaledge.util.BmiCalculator.Calculation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds a structured {@link HealthProfile} from assessment answers.
 * Ports the source project's {@code HealthProfileBuilder} (utils/health_profile.py):
 * BMI computation, per-dimension health scoring, risk-factor detection and the
 * counsellor-recommendation heuristic.
 */
@SuppressWarnings("null")
@Service
public class HealthProfileService {

    public static final String KEY_WEIGHT = "weight_kg";
    public static final String KEY_HEIGHT = "height_cm";
    public static final String KEY_AGE_GROUP = "age_group";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_ACTIVITY = "activity_level";
    public static final String KEY_GOALS = "health_goals";
    public static final String KEY_DIETARY = "dietary_preferences";
    public static final String KEY_ALLERGIES = "allergies";
    public static final String KEY_MEDICAL = "medical_conditions";
    public static final String KEY_PREGNANCY = "pregnant_or_nursing";
    public static final String KEY_CHRONIC = "chronic_diseases";
    public static final String KEY_BLOOD_GROUP = "blood_group";
    public static final String KEY_SLEEP = "sleep_hours";
    public static final String KEY_STRESS = "stress_level";
    public static final String KEY_SMOKER = "smoker";
    public static final String KEY_ALCOHOL = "alcohol";

    private final HealthProfileRepository healthProfileRepository;

    public HealthProfileService(HealthProfileRepository healthProfileRepository) {
        this.healthProfileRepository = healthProfileRepository;
    }

    @Transactional
    public HealthProfile buildProfile(User user, AssessmentSession assessment,
                                      List<AssessmentAnswer> answers) {
        Map<String, Object> extracted = extractAnswers(answers);
        Map<String, Object> profile = new LinkedHashMap<>();

        Double weight = doubleValue(extracted.get(KEY_WEIGHT));
        Double height = doubleValue(extracted.get(KEY_HEIGHT));

        Map<String, Object> bmiData = new LinkedHashMap<>();
        bmiData.put("present", weight != null && height != null);
        if (weight != null && height != null) {
            try {
                Calculation calc = BmiCalculator.calculateFromMetric(weight, height);
                bmiData.put("value", round(calc.getBmi(), 2));
                bmiData.put("classification", calc.getClassification().name());
            } catch (IllegalArgumentException e) {
                bmiData.put("value", null);
                bmiData.put("classification", "invalid");
            }
        }
        profile.put("bmi", bmiData);

        Map<String, Object> scores = computeScores(extracted, bmiData);
        profile.put("scores", scores);

        List<String> riskFactors = computeRiskFactors(extracted, bmiData, scores);
        profile.put("risk_factors", riskFactors);

        boolean counsellorRecommended = computeCounsellorRecommended(extracted, bmiData, riskFactors);
        profile.put("counsellor_recommended", counsellorRecommended);

        profile.put("extracted", sanitize(extracted));
        profile.put("built_at_epoch", System.currentTimeMillis() / 1000);

        int version = latestVersionFor(user.getId()) + 1;
        HealthProfile hp = new HealthProfile(assessment, user, profile, counsellorRecommended);
        hp.setVersion(version);
        return healthProfileRepository.save(hp);
    }

    private int latestVersionFor(java.util.UUID userId) {
        return healthProfileRepository.findFirstByUserIdOrderByVersionDesc(userId)
                .map(HealthProfile::getVersion)
                .orElse(0);
    }

    private Map<String, Object> computeScores(Map<String, Object> answers, Map<String, Object> bmiData) {
        Map<String, Object> scores = new LinkedHashMap<>();
        scores.put("nutrition", scoreNutrition(answers));
        scores.put("physical_activity", scoreActivity(answers));
        scores.put("sleep", scoreSleep(answers));
        scores.put("stress", scoreStress(answers));
        scores.put("lifestyle", scoreLifestyle(answers));
        scores.put("overall", computeOverall(scores));
        return scores;
    }

    private double scoreNutrition(Map<String, Object> answers) {
        int score = 60;
        if (answers.containsKey(KEY_GOALS)) {
            score += 10;
        }
        Object dietary = answers.get(KEY_DIETARY);
        if (dietary != null && !isBlankList(dietary)) {
            score += 10;
        }
        if (Boolean.FALSE.equals(booleanValue(answers.get(KEY_ALCOHOL), false))) {
            score += 10;
        }
        return clamp(score);
    }

    private double scoreActivity(Map<String, Object> answers) {
        String activity = stringValue(answers.get(KEY_ACTIVITY));
        if (activity == null) {
            return 50;
        }
        return switch (activity.toLowerCase()) {
            case "sedentary" -> 30;
            case "light" -> 50;
            case "moderate" -> 70;
            case "active" -> 90;
            case "very_active" -> 100;
            default -> 50;
        };
    }

    private double scoreSleep(Map<String, Object> answers) {
        Double hours = doubleValue(answers.get(KEY_SLEEP));
        if (hours == null) {
            return 50;
        }
        if (hours >= 7 && hours <= 9) {
            return 90;
        } else if (hours >= 6 && hours < 7) {
            return 70;
        } else if (hours >= 9 && hours <= 10) {
            return 60;
        }
        return 30;
    }

    private double scoreStress(Map<String, Object> answers) {
        String stress = stringValue(answers.get(KEY_STRESS));
        if (stress == null) {
            return 50;
        }
        return switch (stress.toLowerCase()) {
            case "low" -> 90;
            case "moderate" -> 65;
            case "high" -> 40;
            case "very_high" -> 25;
            default -> 50;
        };
    }

    private double scoreLifestyle(Map<String, Object> answers) {
        int score = 70;
        if (Boolean.TRUE.equals(booleanValue(answers.get(KEY_SMOKER), false))) {
            score -= 25;
        }
        if (Boolean.TRUE.equals(booleanValue(answers.get(KEY_ALCOHOL), false))) {
            score -= 15;
        }
        return clamp(score);
    }

    private double computeOverall(Map<String, Object> scores) {
        double sum = 0;
        int count = 0;
        for (Map.Entry<String, Object> e : scores.entrySet()) {
            if (e.getValue() instanceof Number n && !"overall".equals(e.getKey())) {
                sum += n.doubleValue();
                count++;
            }
        }
        return count == 0 ? 0 : round(sum / count, 2);
    }

    private List<String> computeRiskFactors(Map<String, Object> answers, Map<String, Object> bmiData,
                                            Map<String, Object> scores) {
        List<String> risks = new ArrayList<>();

        Object classification = bmiData.get("classification");
        if ("underweight".equals(classification)) {
            risks.add("underweight");
        } else if ("overweight".equals(classification)) {
            risks.add("overweight");
        } else if (classification instanceof String s && s.startsWith("obese")) {
            risks.add("obesity");
        }

        Object medical = answers.get(KEY_MEDICAL);
        if (medical != null && !isBlankList(medical)) {
            risks.add("medical_condition");
        }
        Object chronic = answers.get(KEY_CHRONIC);
        if (chronic != null && !isBlankList(chronic)) {
            risks.add("chronic_disease");
        }
        if (Boolean.TRUE.equals(booleanValue(answers.get(KEY_PREGNANCY), false))) {
            risks.add("pregnancy");
        }
        if (Boolean.TRUE.equals(booleanValue(answers.get(KEY_SMOKER), false))) {
            risks.add("smoker");
        }
        if (scores.get("sleep") instanceof Number s && s.doubleValue() < 40) {
            risks.add("poor_sleep");
        }
        if (scores.get("stress") instanceof Number s && s.doubleValue() < 40) {
            risks.add("high_stress");
        }
        return risks;
    }

    private boolean computeCounsellorRecommended(Map<String, Object> answers, Map<String, Object> bmiData,
                                                 List<String> riskFactors) {
        if (Boolean.TRUE.equals(booleanValue(answers.get(KEY_PREGNANCY), false))) {
            return true;
        }
        Object classification = bmiData.get("classification");
        if (classification instanceof String c
                && (c.equals("obese_class_ii") || c.equals("obese_class_iii"))) {
            return true;
        }
        return riskFactors.stream().filter(Objects::nonNull).count() >= 2;
    }

    private Map<String, Object> extractAnswers(List<AssessmentAnswer> answers) {
        Map<String, Object> extracted = new LinkedHashMap<>();
        for (AssessmentAnswer answer : answers) {
            extracted.put(answer.getQuestion().getKey(), answer.getAnswerValue());
        }
        return extracted;
    }

    private Map<String, Object> sanitize(Map<String, Object> answers) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : answers.entrySet()) {
            out.put(e.getKey(), e.getValue());
        }
        return out;
    }

    private static Double doubleValue(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        if (value instanceof String s) {
            return s;
        }
        return value == null ? null : String.valueOf(value);
    }

    private static boolean booleanValue(Object value, boolean defaultValue) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s);
        }
        return defaultValue;
    }

    private static boolean isBlankList(Object value) {
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        if (value instanceof String s) {
            return s.isBlank() || "[]".equals(s.trim());
        }
        return true;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    /** Question keys referenced by the health profile builder. */
    public static Set<String> knownKeys() {
        return Set.of(KEY_WEIGHT, KEY_HEIGHT, KEY_AGE_GROUP, KEY_GENDER, KEY_ACTIVITY, KEY_GOALS,
                KEY_DIETARY, KEY_ALLERGIES, KEY_MEDICAL, KEY_PREGNANCY, KEY_CHRONIC, KEY_BLOOD_GROUP,
                KEY_SLEEP, KEY_STRESS, KEY_SMOKER, KEY_ALCOHOL);
    }
}