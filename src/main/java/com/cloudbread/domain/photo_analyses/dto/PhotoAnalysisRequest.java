package com.cloudbread.domain.photo_analyses.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class PhotoAnalysisRequest {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiLabelRequest {
        @NotBlank
        private String label;      // "김치찌개"
        private Double confidence; // 0.0 ~ 1.0 (nullable OK)
    }

    @Data
    public static class ConfirmRequest {
        @NotNull(message = "selectedFoodId is required")
        @JsonAlias({"selected_food_id","selectedFoodId"}) // snake/camel 모두 허용
        private Long selectedFoodId;
    }
}
