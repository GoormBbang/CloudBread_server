package com.cloudbread.domain.chat.nutrients.dto.ai;

import lombok.Data;

@Data
public class AiChatResponse {
    private String response;
    private String session_id;
    private java.util.List<MessageItem> message_history;

    @Data
    public static class MessageItem {
        private String role;
        private String content;
        private String timestamp;
    }
}
