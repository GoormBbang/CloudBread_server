package com.cloudbread.domain.feedback.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.feedback.application.FeedbackService;
import com.cloudbread.domain.feedback.dto.FeedbackResponseDto;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/nutrition")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public BaseResponse<FeedbackResponseDto> createNutritionFeedback(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUser().getId();
        log.info("📩 [FastAPI 피드백 요청] userId={}", userId);

        // ✅ FastAPI 응답 JSON을 그대로 반환
        return BaseResponse.onSuccess(SuccessStatus.FEEDBACK_SUCCESS, feedbackService.generateFeedback(userId));
    }
}
