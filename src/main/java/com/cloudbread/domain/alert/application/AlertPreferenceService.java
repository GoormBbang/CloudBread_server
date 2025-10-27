package com.cloudbread.domain.alert.application;

import com.cloudbread.domain.alert.dto.AlertPreferenceRequest;
import com.cloudbread.domain.alert.dto.AlertPreferenceResponse;

import java.util.Optional;

public interface AlertPreferenceService {
    AlertPreferenceResponse getMyAlertPreferences(Long userId);//알림설정조회
    AlertPreferenceResponse updateMyAlertPreferences(Long userId, AlertPreferenceRequest request);
}
