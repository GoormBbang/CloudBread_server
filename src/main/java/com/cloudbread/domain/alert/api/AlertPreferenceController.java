package com.cloudbread.domain.alert.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.alert.application.AlertPreferenceService;
import com.cloudbread.domain.alert.dto.AlertPreferenceResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/alert-preferences")
@RequiredArgsConstructor
public class AlertPreferenceController {

    private final AlertPreferenceService alertPreferenceService;

    @GetMapping
    public BaseResponse<AlertPreferenceResponse> getMyPreferences(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUser().getId();

        AlertPreferenceResponse response = alertPreferenceService.getMyAlertPreferences(userId);

        return BaseResponse.onSuccess(SuccessStatus.ALERT_SETTING_SUCCESS, response);
    }
}
