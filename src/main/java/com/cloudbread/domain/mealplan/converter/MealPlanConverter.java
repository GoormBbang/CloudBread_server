package com.cloudbread.domain.mealplan.converter;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import com.cloudbread.domain.mealplan.domain.entity.MealPlanItem;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto.SectionDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto.FoodItemDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MealPlanConverter {

    public MealPlanResponseDto toResponseDto(MealPlan mealPlan) {

        Map<String, List<MealPlanItem>> groupedByMealType =
                mealPlan.getMealPlanItems().stream()
                        .collect(Collectors.groupingBy(item -> item.getMealType().name()));


        List<SectionDto> sections = groupedByMealType.entrySet().stream()
                .map(entry -> {
                    String mealType = entry.getKey();
                    List<MealPlanItem> items = entry.getValue();

                    int totalKcal = items.stream()
                            .mapToInt(i -> i.getEstCalories() != null ? i.getEstCalories() : 0)
                            .sum();

                    List<FoodItemDto> itemDtos = items.stream()
                            .map(this::toItemResponse)
                            .collect(Collectors.toList());

                    return new SectionDto(mealType, totalKcal, itemDtos);
                })
                .collect(Collectors.toList());

        return new MealPlanResponseDto(
                mealPlan.getId(),
                mealPlan.getPlanDate().toString(),
                sections
        );
    }


//    private FoodItemDto toItemResponse(MealPlanItem item) {
//        Food food = item.getFood();
//
//        return new FoodItemDto(
//                food.getId(),
//                food.getName(),
//                item.getPortionLabel(),
//                item.getEstCalories(),
//                food.getCategory()
//        );
//    }
private FoodItemDto toItemResponse(MealPlanItem item) {
    return new FoodItemDto(
            item.getFood().getId(),          // DB FK
            item.getFoodName(),              // ✅ FastAPI 원본 name
            item.getPortionLabel(),          // ✅ FastAPI 원본 portion
            item.getEstCalories(),           // ✅ FastAPI 원본 kcal
            item.getFoodCategory()           // ✅ FastAPI 원본 category
    );
}

}
