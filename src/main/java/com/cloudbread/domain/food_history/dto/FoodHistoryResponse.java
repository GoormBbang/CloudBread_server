package com.cloudbread.domain.food_history.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoodHistoryResponse {
    @Getter @Builder
    public static class Created {
        private Long historyId;
        private String mealType;
        private int intakePercent;
        private LocalDateTime eatenAt;
        private SelectedFood selectedFood;
    }

    @Getter @Builder
    public static class SelectedFood {
        private Long foodId;
        private String name;
        // 현재 ERD/요구 응답에는 imageUrl 불포함(원하면 필드 추가해서 확장 가능)
    }

    // ───────────────────────────────
    //  캘린더 상세 조회 DTOs
    // ───────────────────────────────
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarDailySummaryDto {

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        private Integer totalCalories; // 하루 총 칼로리
        private NutritionTotalsDto nutritionTotals; // 탄단지당
        private List<MealSummaryDto> meals; // 끼니별 정보

        // ✅ 변경: 문자열 → 리스트(JSON)
        private List<MealIntakeLevelDto> intakeMessages;

        public static CalendarDailySummaryDto createEmpty(LocalDate date) {
            return CalendarDailySummaryDto.builder()
                    .date(date)
                    .totalCalories(0)
                    .nutritionTotals(NutritionTotalsDto.createEmpty())
                    .meals(new ArrayList<>())
                    .intakeMessages(List.of()) // ✅ 빈 리스트 반환
                    .build();
        }

        // ✅ 새로 추가: 끼니별 섭취 수준 리스트
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MealIntakeLevelDto {
            private String mealType;  // "아침", "점심", "저녁"
            private String level;     // "적게", "적당히", "많이"
        }
    }

    // 하루 전체 영양소 합계 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionTotalsDto {
        private Integer carbs;   // 탄수화물(g)
        private Integer protein; // 단백질(g)
        private Integer fat;     // 지방(g)
        private Integer sugar;   // 당류(g)

        public static NutritionTotalsDto createEmpty() {
            return NutritionTotalsDto.builder()
                    .carbs(0)
                    .protein(0)
                    .fat(0)
                    .sugar(0)
                    .build();
        }
    }

    //  🍽️ 끼니별 요약 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealSummaryDto {
        private String mealType;  // BREAKFAST, LUNCH, DINNER
        private Integer totalCalories;
        private List<FoodItemDto> foods;
    }

    // 🍙 음식 단위 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private String foodName;
        private String category;
        private Integer calories;
    }

}
