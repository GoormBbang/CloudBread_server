package com.cloudbread.domain.chat.main.application.session_store;

import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfile;

import java.time.Instant;

public record AiSessionContext(
        Long userId,
        String topic,
        UserProfile userProfile,
        Instant createdAt

) { }
