package com.cloudbread.domain.chat.main.dto;

import lombok.*;

import java.util.List;
public class AiGeneralChatResponseGen {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionCreatedGen {
        private String sessionId;
        private String topic;
        private String expiresAt; // ISO-8601
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MessageGen {
        private String sessionId;
        private String topic;
        private String response;
        private List<HistoryItemGen> history;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HistoryItemGen {
        private String role;
        private String content;
        private String timestamp;
    }
}
