package com.cloudbread.domain.alert.application;

import com.cloudbread.domain.alert.domain.entity.AlertPreference;
import com.cloudbread.domain.alert.domain.repository.AlertPreferenceRepository;
import com.cloudbread.domain.alert.dto.AlertPreferenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertPreferenceServiceImpl implements AlertPreferenceService {

    private final AlertPreferenceRepository alertPreferenceRepository;

    @Override
    @Transactional // DB insert 허용
    public AlertPreferenceResponse getMyAlertPreferences(Long userId) {
        // ✅ 기존 설정 조회
        Optional<AlertPreference> optionalPref = alertPreferenceRepository.findByUserId(userId);

        // ✅ 없으면 기본값(true, true)로 생성
        AlertPreference preference = optionalPref.orElseGet(() -> {
            AlertPreference newPref = AlertPreference.builder()
                    .userId(userId)
                    .mealRecoEnabled(true)
                    .nutrientAlertEnabled(true)
                    .updatedAt(LocalDateTime.now())
                    .build();
            return alertPreferenceRepository.save(newPref);
        });

        // ✅ DTO로 변환 후 반환
        return AlertPreferenceResponse.builder()
                .userId(preference.getUserId())
                .mealRecoEnabled(preference.isMealRecoEnabled())
                .nutrientAlertEnabled(preference.isNutrientAlertEnabled())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
