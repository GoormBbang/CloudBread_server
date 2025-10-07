package com.cloudbread.domain.food_history.application;

import com.cloudbread.domain.food_history.dto.FoodHistoryCalendarDto;
import com.cloudbread.domain.food_history.dto.FoodHistoryRequest;
import com.cloudbread.domain.food_history.dto.FoodHistoryResponse;

import java.time.LocalDate;

public interface FoodHistoryService {
    FoodHistoryResponse.Created create(Long userId, FoodHistoryRequest.Create req);

    // 월별 식단 기록 조회 (캘린더용)
    FoodHistoryCalendarDto getMonthlyCalendar(Long userId, Integer year, Integer month);

    /**
     * 특정 날짜 상세 식단 조회 (캘린더 상세)
     */
    FoodHistoryResponse.CalendarDailySummaryDto getDailySummary(Long userId, LocalDate date);
}
