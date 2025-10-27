package com.cloudbread.domain.alert.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.alert.application.AlertPreferenceService;
import com.cloudbread.domain.alert.dto.AlertPreferenceRequest;
import com.cloudbread.domain.alert.dto.AlertPreferenceResponse;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me/alert-preferences")
@RequiredArgsConstructor
public class AlertPreferenceController {

    private final AlertPreferenceService alertPreferenceService;

    //알림 설정 조회
    @GetMapping
    public BaseResponse<AlertPreferenceResponse> getMyPreferences(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUser().getId();

        AlertPreferenceResponse response = alertPreferenceService.getMyAlertPreferences(userId);

        return BaseResponse.onSuccess(SuccessStatus.ALERT_SETTING_SUCCESS, response);
    }

    //알림 설정 수정
    @PutMapping
    public BaseResponse<?> updateMyPreferences(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestBody AlertPreferenceRequest request
    ) {
        Long userId = principal.getUser().getId();

        AlertPreferenceResponse response =
                alertPreferenceService.updateMyAlertPreferences(userId, request);

        if (response == null) { // 없으면 컨트롤러에서 실패 응답
            return BaseResponse.onFailure(
                    ErrorStatus._BAD_REQUEST,
                    "해당 사용자의 알림 설정 정보를 찾을 수 없습니다."
            );
        }

        return BaseResponse.onSuccess(SuccessStatus.ALERT_SETTING_UPDATE_SUCCESS, response);
    }


}
