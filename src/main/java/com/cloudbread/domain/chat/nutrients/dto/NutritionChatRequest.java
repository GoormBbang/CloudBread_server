package com.cloudbread.domain.chat.nutrients.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class NutritionChatRequest {
    @Data
    public static class CreateSession {
        @NotNull
        @JsonAlias({"foodId","food_id"})
        private Long foodId; // 필수

    }

    @Data
    public static class SendMessage {
        @NotBlank
        @JsonAlias({"sessionId","session_id"})
        private String sessionId;

        @NotBlank
        private String message;
    }
}
