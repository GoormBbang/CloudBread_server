package com.cloudbread.domain.chat.nutrients.application;

import com.cloudbread.domain.chat.nutrients.dto.NutritionChatRequest;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatResponse;

public interface NutritionChatService {
    NutritionChatResponse.SessionCreated createSession(Long userId, NutritionChatRequest.CreateSession req);
    NutritionChatResponse.Message send(Long userId, NutritionChatRequest.SendMessage req);
}
