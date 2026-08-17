package com.vitaledge.util;

import java.util.Objects;

/**
 * BMI (Body Mass Index) calculation utility.
 *
 * <p>BMI = weight_kg / height_m^2</p>
 *
 * <p>This class is the authoritative source for BMI calculations in the application.
 * All BMI logic should go through this class to avoid duplication.</p>
 */
public final class BmiCalculator {

    private BmiCalculator() {
    }

    /** WHO BMI classification thresholds. */
    public static final double UNDERWEIGHT_THRESHOLD = 18.5;
    public static final double NORMAL_THRESHOLD = 24.9;
    public static final double OVERWEIGHT_THRESHOLD = 29.9;
    public static final double OBESE_I_THRESHOLD = 34.9;
    public static final double OBESE_II_THRESHOLD = 39.9;

    /** Validation limits. */
    public static final double MIN_HEIGHT_CM = 50.0;
    public static final double MAX_HEIGHT_CM = 250.0;
    public static final double MIN_WEIGHT_KG = 2.0;
    public static final double MAX_WEIGHT_KG = 300.0;

    /** Conversion factors. */
    public static final double CM_TO_M = 0.01;

    public static final class Calculation {
        private final double bmi;
        private final BmiClassification classification;
        private final double heightCm;
        private final double weightKg;

        public Calculation(double bmi, BmiClassification classification, double heightCm, double weightKg) {
            if (bmi < 0) {
                throw new IllegalArgumentException("BMI cannot be negative: " + bmi);
            }
            if (Double.isInfinite(bmi) || Double.isNaN(bmi)) {
                throw new IllegalArgumentException("BMI must be a finite number: " + bmi);
            }
            this.bmi = bmi;
            this.classification = Objects.requireNonNull(classification, "classification");
            this.heightCm = heightCm;
            this.weightKg = weightKg;
        }

        public double getBmi() {
            return bmi;
        }

        public BmiClassification getClassification() {
            return classification;
        }

        public double getHeightCm() {
            return heightCm;
        }

        public double getWeightKg() {
            return weightKg;
        }

        public boolean isUnderweight() {
            return classification == BmiClassification.underweight;
        }

        public boolean isNormal() {
            return classification == BmiClassification.normal;
        }

        public boolean isOverweight() {
            return switch (classification) {
                case overweight, obese_class_i, obese_class_ii, obese_class_iii -> true;
                default -> false;
            };
        }

        public boolean isObese() {
            return switch (classification) {
                case obese_class_i, obese_class_ii, obese_class_iii -> true;
                default -> false;
            };
        }
    }

    /**
     * Calculate BMI from metric units (kg and cm) with validation.
     *
     * @param weightKg weight in kilograms
     * @param heightCm height in centimeters
     * @return BMI calculation result with classification
     * @throws IllegalArgumentException if inputs are invalid or out of range
     */
    public static Calculation calculateFromMetric(double weightKg, double heightCm) {
        validateInputs(weightKg, heightCm);
        double heightM = heightCm * CM_TO_M;
        double bmiValue = weightKg / (heightM * heightM);
        return new Calculation(bmiValue, classifyBmi(bmiValue), heightCm, weightKg);
    }

    private static void validateInputs(double weight, double height) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive: " + weight);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive: " + height);
        }
        if (weight < MIN_WEIGHT_KG || weight > MAX_WEIGHT_KG) {
            throw new IllegalArgumentException(
                    "Weight out of range (" + MIN_WEIGHT_KG + "-" + MAX_WEIGHT_KG + " kg): " + weight);
        }
        if (height < MIN_HEIGHT_CM || height > MAX_HEIGHT_CM) {
            throw new IllegalArgumentException(
                    "Height out of range (" + MIN_HEIGHT_CM + "-" + MAX_HEIGHT_CM + " cm): " + height);
        }
    }

    private static BmiClassification classifyBmi(double bmi) {
        if (bmi < UNDERWEIGHT_THRESHOLD) {
            return BmiClassification.underweight;
        } else if (bmi <= NORMAL_THRESHOLD) {
            return BmiClassification.normal;
        } else if (bmi <= OVERWEIGHT_THRESHOLD) {
            return BmiClassification.overweight;
        } else if (bmi <= OBESE_I_THRESHOLD) {
            return BmiClassification.obese_class_i;
        } else if (bmi <= OBESE_II_THRESHOLD) {
            return BmiClassification.obese_class_ii;
        }
        return BmiClassification.obese_class_iii;
    }
}