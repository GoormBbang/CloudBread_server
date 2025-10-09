package com.cloudbread.domain.notifiaction.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.notifiaction.application.NotificationService;
import com.cloudbread.domain.notifiaction.dto.NotificationDtos;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 알림 목록/삭제
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {
    private final NotificationService notificationService;

    /** 알림 목록 조회 */
    @GetMapping
    public BaseResponse<Map<String, Object>> list(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        var items = notificationService.list(principal.getUserId(), limit);
        return BaseResponse.onSuccess(SuccessStatus.NOTIFICATION_LIST_SUCCESS, Map.of("items", items));
    }

    /** 알림 전체 삭제 (소프트 삭제) */
    @DeleteMapping
    public BaseResponse<NotificationDtos.DeleteAllRes> deleteAll(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        int deleted = notificationService.softDeleteAll(principal.getUserId());
        return BaseResponse.onSuccess(SuccessStatus.NOTIFICATION_DELETE_SUCCESS,
                NotificationDtos.DeleteAllRes.builder()
                        .deletedCount(deleted)
                        .build());
    }
}
