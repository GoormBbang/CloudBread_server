package com.cloudbread.domain.chat.nutrients.application.session_store;

public interface SessionStore {
    void put(String sessionId, SessionContext ctx, long ttlSeconds);
    SessionContext require(Long userId, String sessionId);
    SessionContext get(String sessionId);
    void remove(String sessionId);
}
