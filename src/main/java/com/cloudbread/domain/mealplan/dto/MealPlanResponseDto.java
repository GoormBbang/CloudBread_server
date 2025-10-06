package com.cloudbread.domain.mealplan.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MealPlanResponseDto {

    @JsonProperty("planId")
    private Long planId;

    @JsonProperty("planDate")
    private String planDate;
    @JsonProperty("sections")
    private List<SectionDto> sections;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionDto {

        @JsonProperty("mealType")
        private String mealType;
        @JsonProperty("totalKcal")
        private int totalKcal;
        @JsonProperty("items")
        private List<FoodItemDto> items;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodItemDto {

        @JsonProperty("foodId")
        private Long foodId;
        @JsonProperty("name")
        private String name;
        @JsonProperty("portionLabel")
        private String portionLabel;
        @JsonProperty("estCalories")
        private int estCalories;
        @JsonProperty("foodCategory")
        private String foodCategory; // 음식 카테고리
    }

}
