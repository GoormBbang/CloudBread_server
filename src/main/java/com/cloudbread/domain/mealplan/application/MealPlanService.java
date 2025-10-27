package com.cloudbread.domain.mealplan.application;

import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import jakarta.annotation.Nullable;

import java.time.LocalDate;

public interface MealPlanService {
    MealPlanResponseDto refreshMealPlan(Long userId);

    MealPlanResponseDto getTodayMealPlan(Long userId);

}
