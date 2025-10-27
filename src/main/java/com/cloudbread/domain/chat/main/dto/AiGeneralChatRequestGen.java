package com.cloudbread.domain.chat.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AiGeneralChatRequestGen {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateSessionGen {
        @NotBlank
        private String topic; // FOOD_INFO | PREGNANCY_DRUG | PREGNANCY | FREE
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendMessageGen {
        @NotBlank
        private String sessionId;
        @NotBlank
        private String topic;   // 매 요청에 들어와서 세션 topic을 덮어씀
        @NotBlank
        private String message;
    }
}
