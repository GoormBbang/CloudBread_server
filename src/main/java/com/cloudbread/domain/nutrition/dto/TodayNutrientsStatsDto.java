package com.cloudbread.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// TodayNutrientsStatsDto.java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayNutrientsStatsDto {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonProperty("nutrients")
    private List<NutrientDetail> nutrients;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutrientDetail {
        private String name;          // 영양소 이름
        private Integer value;        // 권장 대비 %  ← 정수
        private BigDecimal unit;      // 실제 섭취량  ← 반올림 보존
    }

    public static TodayNutrientsStatsDto createEmpty(Long userId, LocalDate date) {
        return TodayNutrientsStatsDto.builder()
                .userId(userId)
                .createdAt(date)
                .nutrients(List.of())
                .build();
    }

    private int totalCalories;
    private String comment;
    private Double lackedValue;
    private String lackedNutrient;
}
