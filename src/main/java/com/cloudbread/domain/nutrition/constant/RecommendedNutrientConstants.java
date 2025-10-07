package com.cloudbread.domain.nutrition.constant;

import java.util.Map;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.Map.entry;

public class RecommendedNutrientConstants {

    public static final Map<String, Map<String, Double>> RECOMMENDED_NUTRIENTS = Map.ofEntries(
            entry("SODIUM", Map.of(
                    "EARLY", 2000.0, "MID", 2000.0, "LATE", 2000.0
            )),
            entry("MOISTURE", Map.of(
                    "EARLY", 1800.0, "MID", 2000.0, "LATE", 2500.0
            )),
            entry("SUGARS", Map.of(
                    "EARLY", 50.0, "MID", 50.0, "LATE", 50.0
            )),
            entry("PROTEINS", Map.of(
                    "EARLY", 60.0, "MID", 70.0, "LATE", 80.0
            )),
            entry("FOLIC_ACID", Map.of(
                    "EARLY", 700.0, "MID", 700.0, "LATE", 700.0
            )),
            entry("CARBS", Map.of(
                    "EARLY", 325.0, "MID", 375.0, "LATE", 375.0
            )),
            entry("TRANS_FAT", Map.of(
                    "EARLY", 0.0, "MID", 0.0, "LATE", 0.0
            )),
            entry("FATS", Map.of(
                    "EARLY", 50.0, "MID", 60.0, "LATE", 70.0
            )),
            entry("IRON", Map.of(
                    "EARLY", 18.0, "MID", 25.0, "LATE", 25.0
            )),
            entry("CHOLESTEROL", Map.of(
                    "EARLY", 300.0, "MID", 300.0, "LATE", 300.0
            )),
            entry("CALCIUM", Map.of(
                    "EARLY", 1000.0, "MID", 1000.0, "LATE", 1000.0
            )),
            entry("SATURATED_FAT", Map.of(
                    "EARLY", 10.0, "MID", 10.0, "LATE", 10.0
            ))
    );

    public static Double getRecommendedValue(String nutrientName, String pregnancyStage) {
        Map<String, Double> stages = RECOMMENDED_NUTRIENTS.get(nutrientName.toUpperCase());
        if (stages == null) return null;
        return stages.get(pregnancyStage.toUpperCase());
    }
}
