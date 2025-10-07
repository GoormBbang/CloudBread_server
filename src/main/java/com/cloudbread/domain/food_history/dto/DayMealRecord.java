package com.cloudbread.domain.food_history.dto;

import com.cloudbread.domain.user.domain.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DayMealRecord {
    private Integer day;
    private MealType mealType;
}
