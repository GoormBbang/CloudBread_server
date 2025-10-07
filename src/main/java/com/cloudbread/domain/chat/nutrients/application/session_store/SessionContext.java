package com.cloudbread.domain.chat.nutrients.application.session_store;

import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfile;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatResponse;

import java.time.Instant;

public record SessionContext(
        Long userId,
        NutritionChatResponse.SelectedFoodSummary food,
        UserProfile userProfile,
        Instant createdAt

) { }
