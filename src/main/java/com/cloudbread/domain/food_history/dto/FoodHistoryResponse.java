package com.cloudbread.domain.food_history.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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
}
