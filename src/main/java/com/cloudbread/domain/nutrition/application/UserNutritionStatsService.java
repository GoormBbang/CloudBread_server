package com.cloudbread.domain.nutrition.application;

import com.cloudbread.domain.nutrition.dto.NutritionBalanceResponse;
import com.cloudbread.domain.nutrition.dto.TodayNutrientsStatsDto;

import java.time.LocalDate;
import java.util.List;

public interface UserNutritionStatsService {

    /**
     * 오늘의 영양 통계 계산
     * @param userId 사용자 ID
     * @return 영양소 섭취 통계 DTO
     */
    TodayNutrientsStatsDto calculateTodayStats(Long userId);


    List<TodayNutrientsStatsDto> getTodaySummary(Long userId, LocalDate date);//영양요약
    NutritionBalanceResponse getNutritionBalance(Long userId, LocalDate date);//영양 밸런스

}