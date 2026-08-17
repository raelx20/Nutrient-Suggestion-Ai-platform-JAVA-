package com.vitaledge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vitaledge.util.BmiCalculator.Calculation;
import org.junit.jupiter.api.Test;

class BmiCalculatorTest {

    @Test
    void classifiesNormalRange() {
        Calculation calc = BmiCalculator.calculateFromMetric(70, 175);
        assertEquals(22.86, calc.getBmi(), 0.01);
        assertEquals(BmiClassification.normal, calc.getClassification());
        assertEquals(22.86, calc.getBmi(), 0.01);
    }

    @Test
    void classifiesUnderweight() {
        Calculation calc = BmiCalculator.calculateFromMetric(50, 180);
        assertEquals(BmiClassification.underweight, calc.getClassification());
        assertEquals(15.43, calc.getBmi(), 0.01);
    }

    @Test
    void classifiesOverweight() {
        Calculation calc = BmiCalculator.calculateFromMetric(85, 175);
        assertEquals(BmiClassification.overweight, calc.getClassification());
    }

    @Test
    void classifiesObeseClasses() {
        assertEquals(BmiClassification.obese_class_i, BmiCalculator.calculateFromMetric(95, 170).getClassification());
        assertEquals(BmiClassification.obese_class_ii, BmiCalculator.calculateFromMetric(110, 170).getClassification());
        assertEquals(BmiClassification.obese_class_iii, BmiCalculator.calculateFromMetric(130, 170).getClassification());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculateFromMetric(0, 175));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculateFromMetric(70, 0));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculateFromMetric(-5, 175));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculateFromMetric(400, 175));
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculateFromMetric(70, 10));
    }

    @Test
    void classificationHelpers() {
        assertTrue(BmiCalculator.calculateFromMetric(50, 180).isUnderweight());
        assertTrue(BmiCalculator.calculateFromMetric(85, 175).isOverweight());
        assertTrue(BmiCalculator.calculateFromMetric(110, 170).isObese());
        assertFalse(BmiCalculator.calculateFromMetric(70, 175).isObese());
    }

    private static void assertTrue(boolean value) {
        org.junit.jupiter.api.Assertions.assertTrue(value);
    }

    private static void assertFalse(boolean value) {
        org.junit.jupiter.api.Assertions.assertFalse(value);
    }
}