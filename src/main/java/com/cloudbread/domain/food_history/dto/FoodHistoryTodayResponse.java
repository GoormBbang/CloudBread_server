package com.cloudbread.domain.food_history.dto;

//기록/오늘먹은 음식 조회용 DTO
import com.cloudbread.domain.user.domain.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodHistoryTodayResponse {

    private LocalDate date;
    private Map<MealType, List<FoodItemDto>> meal_type;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private Long foodId;
        private String name;
        private Integer calories;
        private String imageUrl;
    }

    public static FoodHistoryTodayResponse empty(LocalDate date) {
        return FoodHistoryTodayResponse.builder()
                .date(date)
                .meal_type(Map.of())
                .build();
    }
}

