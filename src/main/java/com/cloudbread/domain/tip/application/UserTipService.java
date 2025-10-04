package com.cloudbread.domain.tip.application;

import com.cloudbread.domain.user.dto.UserResponseDto;

public interface UserTipService {
    UserResponseDto.TipResponse getMyTips();
    UserResponseDto.TipResponse getBabyTips();
    UserResponseDto.TipResponse getMomTips();
    UserResponseDto.TipResponse getNutritionTips();
}
