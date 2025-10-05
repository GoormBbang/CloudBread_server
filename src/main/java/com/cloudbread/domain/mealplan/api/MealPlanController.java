//package com.cloudbread.domain.mealplan.api;
//
//import com.cloudbread.auth.oauth2.CustomOAuth2User;
//import com.cloudbread.domain.mealplan.application.MealPlanService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/meal-plans")
//@RequiredArgsConstructor
//public class MealPlanController {
//
//    private final MealPlanService mealPlanService;
//
//    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshMealPlan(@AuthenticationPrincipal CustomOAuth2User userDetails) {
//        // ✅ 로그인된 사용자 정보에서 PK 추출
//        Long userId = userDetails.getUser().getId(); // 또는 userDetails.getUserId()
//
//        // ✅ 서비스 호출 (AI 추천 식단 생성)
//        mealPlanService.refreshMealPlan(userId);
//
//        return ResponseEntity.ok("AI 추천 식단 생성 요청 완료 ✅");
//    }
//}


package com.cloudbread.domain.mealplan.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.mealplan.application.MealPlanService;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
