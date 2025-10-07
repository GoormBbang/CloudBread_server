package com.cloudbread.domain.chat.nutrients.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.chat.nutrients.application.NutritionChatService;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatRequest;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition-chat")
@RequiredArgsConstructor
public class NutritionChatRestController {
    private final NutritionChatService nutritionChatService;

    // 세션 생성
    @PostMapping("/session")
    public BaseResponse<NutritionChatResponse.SessionCreated> createSession(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NutritionChatRequest.CreateSession req
    ) {
        var res = nutritionChatService.createSession(principal.getUserId(), req);
        return BaseResponse.onSuccess(SuccessStatus.SESSION_CREATED_SUCCESS, res);
    }


    // 메시지 전송
    @PostMapping("/message")
    public BaseResponse<NutritionChatResponse.Message> send(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NutritionChatRequest.SendMessage req
    ) {
        var res = nutritionChatService.send(principal.getUserId(), req);
        return BaseResponse.onSuccess(SuccessStatus._OK, res);
    }
}
