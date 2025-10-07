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
import com.cloudbread.global.common.response.BaseResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * [1단계] 오늘의 영양소 섭취 통계 조회 API
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Slf4j
public class UserNutritionController {

    private final UserNutritionStatsService nutritionStatsService;

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

}

