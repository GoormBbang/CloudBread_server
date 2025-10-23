package com.cloudbread.domain.notifiaction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NutrientTotal {
    private String nutrientKey;    // Nutrient.name (e.g., "FOLIC_ACID")
    private Double totalAmount;   // 당일 총 섭취량 (value * intakePercent/100)
}