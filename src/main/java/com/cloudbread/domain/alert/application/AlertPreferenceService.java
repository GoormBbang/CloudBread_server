package com.cloudbread.domain.alert.application;

import com.cloudbread.domain.alert.dto.AlertPreferenceResponse;

import java.util.Optional;

public interface AlertPreferenceService {
    AlertPreferenceResponse getMyAlertPreferences(Long userId);
}
