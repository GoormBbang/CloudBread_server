package com.cloudbread.domain.notifiaction.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.notifiaction.application.NotificationSseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 구독 엔드포인트
 * GET /api/notifications/subscribe?token=... (쿼리 토큰 인증)
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationSseService notificationSseService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        Long userId = principal.getUserId();
        return notificationSseService.subscribe(userId, lastEventId);
    }
}