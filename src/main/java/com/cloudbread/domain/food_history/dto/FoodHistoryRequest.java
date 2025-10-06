package com.cloudbread.domain.food_history.dto;

import com.cloudbread.domain.user.domain.enums.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class FoodHistoryRequest {

    @Data
    public static class Create {
        @NotNull
        @com.fasterxml.jackson.annotation.JsonAlias({"foodId","food_id"})
        private Long foodId;

        @com.fasterxml.jackson.annotation.JsonAlias({"photoAnalysisId","photo_analysis_id"})
        private Long photoAnalysisId; // 선택

        @NotNull
        @com.fasterxml.jackson.annotation.JsonAlias({"mealType","meal_type"})
        private MealType mealType; // BREAKFAST | LUNCH | DINNER

        @Min(0) @Max(100)
        @com.fasterxml.jackson.annotation.JsonAlias({"intakePercent","intake_percent"})
        private int intakePercent;

        @com.fasterxml.jackson.annotation.JsonAlias({"eatenAt","eaten_at"})
        private LocalDateTime eatenAt; // 선택
    }
}