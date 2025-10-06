package com.cloudbread.domain.mealplan.application;

import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;

public interface MealPlanService {
    MealPlanResponseDto refreshMealPlan(Long userId);
}
