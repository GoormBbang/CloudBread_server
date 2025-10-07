//package com.cloudbread.domain.nutrition.api;
//
//import com.cloudbread.domain.nutrition.application.UserNutritionStatsService;
//import com.cloudbread.domain.nutrition.dto.TodayNutrientsStatsDto;
//import com.cloudbread.global.common.response.BaseResponse;
//import com.cloudbread.global.common.code.status.SuccessStatus;
//import com.cloudbread.auth.oauth2.CustomOAuth2User;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/users/me")
//@RequiredArgsConstructor
//@Slf4j
//public class UserNutritionController {
//
//    private final UserNutritionStatsService nutritionStatsService;
//
//    @GetMapping("/today-nutrients-stats")
//    public BaseResponse<TodayNutrientsStatsDto> getTodayNutrientsStats(
//            @AuthenticationPrincipal CustomOAuth2User userDetails
//    ) {
//        Long userId = userDetails.getUser().getId();
//        TodayNutrientsStatsDto result = nutritionStatsService.calculateTodayStats(userId);
//        return BaseResponse.onSuccess(SuccessStatus.NUTRITION_STATS_SUCCESS, result);
//    }
//}
package com.cloudbread.domain.nutrition.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.nutrition.application.UserNutritionStatsService;
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

//    @GetMapping("/nutrition/summary")
//    public BaseResponse<?> getTodaySummary(
//            @AuthenticationPrincipal CustomOAuth2User userDetails,
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
//    ) {
//        if (userDetails == null || userDetails.getUser() == null) {
//            log.warn("[Nutrition] 인증 정보 없음");
//            return BaseResponse.onFailure(ErrorStatus._UNAUTHORIZED, "인증이 필요합니다.", "/api/users/me/nutrition/summary");
//        }
//
//        Long userId = userDetails.getUser().getId();
//        LocalDate targetDate = (date != null) ? date : LocalDate.now();
//
//        log.info("[Nutrition] 요약 요청 userId={}, date={}", userId, targetDate);
//
//        List<TodayNutrientsStatsDto> summaries = nutritionStatsService.getTodaySummary(userId, targetDate);
//
//        if (summaries.isEmpty()) {
//            log.info("[Nutrition] 식단 기록 없음");
//            return BaseResponse.onFailure(ErrorStatus.NUTRITION_SUMMARY_EMPTY, "식단 기록이 없습니다.");
//        }
//
//        TodayNutrientsStatsDto dto = summaries.get(0);
//        if (dto.getLackedNutrient() == null || dto.getLackedValue() == 0.0) {
//            log.info("[Nutrition] 부족한 영양소 없음 🎉");
//            return BaseResponse.onFailure(ErrorStatus.NO_DEFICIENT_NUTRIENT, "부족한 영양소가 없습니다.");
//        }
//
//        return BaseResponse.onSuccess(SuccessStatus.NUTRITION_SUMMARY_SUCCESS, summaries);
//    }
@GetMapping("nutrition/summary")
public BaseResponse<?> getTodaySummary(
        @AuthenticationPrincipal CustomOAuth2User principal,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {
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

    // ✅ 식단 기록이 없을 때
    if (stats == null || stats.isEmpty()) {
        log.warn("[Nutrition] 식단 기록 없음 - userId={}, date={}", userId, targetDate);
        return BaseResponse.onFailure(
                ErrorStatus.NUTRITION_SUMMARY_FAIL, // ✅ 실패 코드로 변경
                "식단 기록 없음."
        );
    }

    // ✅ 부족한 영양소가 없을 때
    TodayNutrientsStatsDto dto = stats.get(0);
    if (dto.getLackedNutrient() == null || dto.getLackedValue() == 0.0) {
        log.info("[Nutrition] 부족한 영양소 없음 🎉 - userId={}, date={}", userId, targetDate);
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


}

