package com.cloudbread.domain.nutrition.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.nutrition.application.UserNutritionStatsService;
import com.cloudbread.domain.nutrition.constant.RecommendedNutrientConstants;
import com.cloudbread.domain.nutrition.dto.NutritionBalanceResponse;
import com.cloudbread.domain.nutrition.dto.TodayNutrientsStatsDto;
import com.cloudbread.domain.nutrition.dto.TodayNutrientsSummaryResponse;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.common.response.BaseResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;
import java.util.List;

/**
 * [1단계] 오늘의 영양소 섭취 통계 조회 API
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Slf4j
public class UserNutritionController {

    private final UserNutritionStatsService nutritionStatsService;
    private final UserNutritionStatsService userNutritionStatsService;

    //오늘의 영양 분석 조회
    @GetMapping("/today-nutrients-stats")
    public BaseResponse<TodayNutrientsStatsDto> getTodayNutrientsStats(
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        log.info("[1단계] API 호출 - userId: {}", userId);

        TodayNutrientsStatsDto result = nutritionStatsService.calculateTodayStats(userId);

        // ✅ nutrients 리스트 기반으로 로깅
        log.info("[5단계] API 응답 - 총 {}개 영양소 분석 결과", result.getNutrients().size());
        result.getNutrients().forEach(n ->
                log.info("  ▶ {}: {:.1f}% (섭취량: {:.2f})", n.getName(), n.getValue(), n.getUnit())
        );

        return BaseResponse.onSuccess(SuccessStatus.NUTRITION_STATS_SUCCESS, result);
    }


    //오늘의 영양 요약
    @GetMapping("nutrition/summary")
    public BaseResponse<?> getTodaySummary(
        @AuthenticationPrincipal CustomOAuth2User principal,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    if (principal == null || principal.getUser() == null) {
        log.warn("[Nutrition] 인증 정보 없음");
        return BaseResponse.onFailure(
                ErrorStatus._UNAUTHORIZED,
                "인증이 필요합니다.",
                "/api/users/me/nutrition/summary"
        );
    }

    Long userId = principal.getUserId();
    LocalDate targetDate = (date != null) ? date : LocalDate.now();

    log.info("📊 [영양 요약 조회] userId={}, date={}", userId, targetDate);

    List<TodayNutrientsStatsDto> stats = userNutritionStatsService.getTodaySummary(userId, targetDate);

    // 식단 기록이 없을 때
    if (stats == null || stats.isEmpty()) {
        log.warn("[Nutrition] 식단 기록 없음 - userId={}, date={}", userId, targetDate);
        return BaseResponse.onFailure(
                ErrorStatus.NUTRITION_SUMMARY_FAIL,
                "식단 기록 없음."
        );
    }

    // 부족한 영양소가 없을 때
    TodayNutrientsStatsDto dto = stats.get(0);
    if (dto.getLackedNutrient() == null || dto.getLackedValue() == 0.0) {
        log.info("[Nutrition] 부족한 영양소 없음 - userId={}, date={}", userId, targetDate);
        return BaseResponse.onFailure(
                ErrorStatus.NUTRITION_SUMMARY_FAIL,
                "부족한 영양소 없음."
        );
    }

    List<TodayNutrientsSummaryResponse> responseList = stats.stream()
            .map(TodayNutrientsSummaryResponse::from)
            .toList();

    return BaseResponse.onSuccess(SuccessStatus.NUTRITION_SUMMARY_SUCCESS, responseList);
    }

    @GetMapping("nutrition/balance") // 영양 밸런스 조회
    public BaseResponse<?> getNutritionBalance(
        @AuthenticationPrincipal CustomOAuth2User userDetails,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
    Long userId = userDetails.getUser().getId();
    LocalDate targetDate = (date != null) ? date : LocalDate.now();
    log.info("[탄단지 밸런스 분석 요청] userId={}, date={}", userId, targetDate);

    try {
        NutritionBalanceResponse result = userNutritionStatsService.getNutritionBalance(userId, targetDate);

        // ✅ 1) 기록 없음 → 0값 바디 만들어서 실패 응답으로 반환
        if (result == null || result.getBalance() == null || result.getBalance().isEmpty()) {
            log.warn("[탄단지 밸런스 실패] 섭취 기록 없음 - userId={}, date={}", userId, targetDate);

            // 권장량은 사용자 임신 단계 기준으로 계산 (userDetails에서 dueDate 사용)
            LocalDate dueDate = userDetails.getUser().getDueDate();
            String stage = resolvePregnancyStage(dueDate);
            Map<String, Double> rec = RecommendedNutrientConstants.getRecommendedValues(stage);

            NutritionBalanceResponse emptyBody = NutritionBalanceResponse.builder()
                    .date(targetDate)
                    .carbs(BigDecimal.ZERO,   rec.getOrDefault("CARBS", 0.0))
                    .protein(BigDecimal.ZERO, rec.getOrDefault("PROTEIN", 0.0))
                    .fat(BigDecimal.ZERO,     rec.getOrDefault("FAT", 0.0))
                    .build();

            // 실패 응답(에러코드 유지) + 0값 바디 포함
            return BaseResponse.onFailure(
                    ErrorStatus.NUTRITION_BALANCE_FAIL,
                    emptyBody
            );
        }

        // ✅ 2) 정상 응답
        log.info("[탄단지 밸런스 분석 완료] carbs={}g, protein={}g, fat={}g",
                result.getBalance().get("carbs").getActual(),
                result.getBalance().get("protein").getActual(),
                result.getBalance().get("fat").getActual()
        );

        return BaseResponse.onSuccess(SuccessStatus.NUTRITION_BALANCE_SUCCESS, result);

    } catch (Exception e) {
        // ✅ 3) 예외 → 실패 응답(바디 null)
        log.error("[탄단지 밸런스 분석 중 오류] userId={}, date={}, message={}",
                userId, targetDate, e.getMessage(), e);

        return BaseResponse.onFailure(null, null, e.getMessage());
    }
}

    /** 컨트롤러 내 보조 메서드: 임신 단계 계산 */
    private String resolvePregnancyStage(LocalDate dueDate) {
        if (dueDate == null) return "EARLY"; // 기본값
        LocalDate start = dueDate.minusWeeks(40);
        long weeks = java.time.temporal.ChronoUnit.WEEKS.between(start, LocalDate.now());
        if (weeks <= 13) return "EARLY";
        if (weeks <= 27) return "MIDDLE";
        return "LATE";
    }

}

