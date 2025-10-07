package com.cloudbread.domain.chat.nutrients.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class NutritionChatResponse {
    @Getter
    @Builder
    public static class SessionCreated {
        private String sessionId;
        private SelectedFoodSummary selectedFood;
    }

    @Getter @Builder
    public static class SelectedFoodSummary {
        private Long foodId;
        private String name;
        private String serving;     // 예: "100g"
        private java.math.BigDecimal calories;
        private Map<String,NutrientValue> nutrients; // calories 제외 나머지
    }

    @Getter @Builder
    public static class NutrientValue {
        private java.math.BigDecimal value;
        private String unit; // g | mg | μg
    }

    @Getter @Builder
    public static class Message {
        private String sessionId;
        private String response;
        private List<HistoryItem> history;
    }

    @Getter @Builder
    public static class HistoryItem {
        private String role;      // "user" | "assistant"
        private String content;
        private String timestamp; // FastAPI는 문자열로 줌
    }
}
