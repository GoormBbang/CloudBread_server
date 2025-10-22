package com.cloudbread.domain.mealplan.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.mealplan.application.MealPlanService;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class
MealPlanController {

    private final MealPlanService mealPlanService;

    /**
     * ✅ AI 추천 식단 생성 요청
     * 로그인된 사용자의 정보를 기반으로 FastAPI 호출 및 식단 데이터 저장
     */
    @PostMapping("/refresh")
    public BaseResponse<MealPlanResponseDto> refreshMealPlan(
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        MealPlanResponseDto result = mealPlanService.refreshMealPlan(userId);
        return BaseResponse.onSuccess(SuccessStatus.MEAL_PLAN_CREATE_SUCCESS, result);
    }

    /**
     * 오늘의 AI 추천 식단 조회 (KST 기준)
     * - 있으면 200 + 동일 ResponseBody
     * - 없으면 404 + "오늘은 아직 없으니 생성해달라" 안내 메시지
     */
    @GetMapping("/today")
    public ResponseEntity<BaseResponse<MealPlanResponseDto>> getTodayMealPlan(
            @AuthenticationPrincipal CustomOAuth2User userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        try {
            MealPlanResponseDto result = mealPlanService.getTodayMealPlan(userId);
            return ResponseEntity.ok(
                    BaseResponse.onSuccess(SuccessStatus.MEAL_PLAN_READ_SUCCESS, result)
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.onFailure(
                            ErrorStatus.MEAL_PLAN_TODAY_NOT_FOUND,
                            "오늘 날짜의 추천 식단이 없습니다. POST /api/meal-plans/refresh 엔드포인트로 생성해 주세요."
                    ));
        }
    }
}
