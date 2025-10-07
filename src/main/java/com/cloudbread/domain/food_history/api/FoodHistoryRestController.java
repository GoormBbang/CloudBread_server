package com.cloudbread.domain.food_history.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.food_history.application.FoodHistoryService;
import com.cloudbread.domain.food_history.dto.FoodHistoryCalendarDto;
import com.cloudbread.domain.food_history.dto.FoodHistoryRequest;
import com.cloudbread.domain.food_history.dto.FoodHistoryResponse;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FoodHistoryRestController {
    private final FoodHistoryService foodHistoryService;

    @PostMapping("/food-history")
    public BaseResponse<FoodHistoryResponse.Created> create(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody FoodHistoryRequest.Create req
    ) {
        Long userId = principal.getUserId();
        var res = foodHistoryService.create(userId, req);
        return BaseResponse.onSuccess(SuccessStatus.FOOD_HISTORY_CREATED, res);
    }

    /**
     * 📅 월별 식단 기록 조회 (캘린더용)
     * - 각 날짜별로 사용자가 몇 끼를 기록했는지 카운트 반환
     */
    @GetMapping("/users/me/food-history/calendar")
    public BaseResponse<FoodHistoryCalendarDto> getMonthlyFoodCalendar(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestParam @Min(2020) @Max(2100) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month
    ) {
        Long userId = principal.getUserId();
        //log.info("[캘린더 조회] userId={}, year={}, month={}", userId, year, month);

        FoodHistoryCalendarDto result = foodHistoryService.getMonthlyCalendar(userId, year, month);

        // ✅ 식단 데이터 없을 때 실패 응답
        if (result.getDays() == null || result.getDays().isEmpty()) {
            return BaseResponse.onFailure(ErrorStatus.CALENDAR_GET_EMPTY, result);
        }

        // ✅ 데이터가 존재할 때만 성공
        return BaseResponse.onSuccess(SuccessStatus.CALENDAR_GET_SUCCESS, result);
    }

    /**
     * 📆 특정 날짜 상세 조회 (캘린더 일별 상세)
     * - 끼니별 영양 정보 및 음식 목록 반환
     */
    /**
     * 📆 특정 날짜 상세 조회 (캘린더 일별 상세)
     * - 끼니별 영양 정보 및 음식 목록 반환
     * - 날짜를 클릭했을 때 하단에 표시될 정보
     */
    @GetMapping("/users/me/food-history/calendar-summary")
    public BaseResponse<FoodHistoryResponse.CalendarDailySummaryDto> getDailySummary(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = principal.getUserId();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        log.info("📆 [캘린더 상세 조회] userId={}, date={}", userId, targetDate);

        try {
            FoodHistoryResponse.CalendarDailySummaryDto result = foodHistoryService.getDailySummary(userId, targetDate);
            return BaseResponse.onSuccess(SuccessStatus.CALENDAR_SUMMERY_SUCCESS, result);
        } catch (IllegalArgumentException e) {
            return BaseResponse.onFailure(
                    ErrorStatus.CALENDAR_SUMMARY_FAIL, e.getMessage()
            );
        }
    }
}
