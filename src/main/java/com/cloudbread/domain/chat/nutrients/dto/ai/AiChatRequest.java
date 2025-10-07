package com.cloudbread.domain.chat.nutrients.dto.ai;

import lombok.Data;

import java.util.Map;

@Data
public class AiChatRequest {
    private String session_id;
    private String message;
    /** BE → FastAPI로 넘길 컨텍스트(토픽/프로필/음식) */
    private Map<String, Object> context;
}