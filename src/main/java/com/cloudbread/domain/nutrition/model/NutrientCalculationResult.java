package com.cloudbread.domain.nutrition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NutrientCalculationResult {
    private double folicAcid;
    private double calcium;
    private double iron;
}
