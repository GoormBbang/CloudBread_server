package com.cloudbread.domain.chat.nutrients.application.user_profile;

import java.util.List;

public record UserProfile(
        List<String> healthConditions,
        List<String> diets,
        List<String> allergies
)
{ }
