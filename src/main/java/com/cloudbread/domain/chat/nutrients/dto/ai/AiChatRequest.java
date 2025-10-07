package com.cloudbread.domain.chat.nutrients.dto.ai;

import lombok.Data;

@Data
public class AiChatRequest {
    private String session_id;
    private String message;
    private String system_prompt;
}