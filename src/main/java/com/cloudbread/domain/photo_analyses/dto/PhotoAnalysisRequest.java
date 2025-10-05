package com.cloudbread.domain.photo_analyses.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
