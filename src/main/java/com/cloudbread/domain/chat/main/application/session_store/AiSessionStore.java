package com.cloudbread.domain.chat.main.application.session_store;

import java.util.UUID;

public interface AiSessionStore {
    void put(String sessionId, AiSessionContext ctx, long ttlSeconds);
    AiSessionContext require(Long userId, String sessionId);
    AiSessionContext get(String sessionId);
    void remove(String sessionId);
    void touch(String sessionId, long ttlSeconds);
    void updateTopic(String sessionId, String topic);

    static String newSessionId() { return UUID.randomUUID().toString(); }
}
