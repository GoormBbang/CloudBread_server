package com.cloudbread.domain.chat.main.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.chat.main.application.AiGeneralChatService;

import com.cloudbread.domain.chat.main.dto.AiGeneralChatRequestGen;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatResponseGen;
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
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiGeneralChatRestController {
    private final AiGeneralChatService aiGeneralChatService;

    /** 세션 생성 */
    @PostMapping("/session")
    public BaseResponse<AiGeneralChatResponseGen.SessionCreatedGen> createSession(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody AiGeneralChatRequestGen.CreateSessionGen req
    ) {
        var res = aiGeneralChatService.createSession(principal.getUserId(), req);
        return BaseResponse.onSuccess(SuccessStatus.SESSION_CREATED_SUCCESS, res);
    }

    /** 메시지 전송 */
    @PostMapping("/message")
    public BaseResponse<AiGeneralChatResponseGen.MessageGen> send(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody AiGeneralChatRequestGen.SendMessageGen req
    ) {
        var res = aiGeneralChatService.send(principal.getUserId(), req);
        return BaseResponse.onSuccess(SuccessStatus._OK, res);
    }
}
