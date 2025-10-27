package com.cloudbread.domain.nutrition.dto;

import lombok.*;

/**
 * /api/users/me/nutrition/summary 전용 응답 DTO
 * 기존 TodayNutrientsStatsDto는 다른 API에서 계속 사용 가능
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayNutrientsSummaryResponse {

    private int totalCalories;
    private String comment;
    private Double lackedValue;
    private String lackedNutrient;

    // 기존 TodayNutrientsStatsDto → Response 변환용 정적 메서드
    public static TodayNutrientsSummaryResponse from(TodayNutrientsStatsDto dto) {
        return TodayNutrientsSummaryResponse.builder()
                .totalCalories(dto.getTotalCalories())
                .comment(dto.getComment())
                .lackedValue(dto.getLackedValue())
                .lackedNutrient(dto.getLackedNutrient())
                .build();
    }
}
