package com.cloudbread.domain.chat.nutrients.application.session_store;


import com.cloudbread.domain.chat.nutrients.exception.SessionExpiredException;
import com.cloudbread.domain.chat.nutrients.exception.SessionNotFoundException;
import com.cloudbread.domain.chat.nutrients.exception.SessionOwnerMismatchException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemorySessionStore implements SessionStore {
    private static class Entry {
        SessionContext ctx;
        Instant expireAt;
    }
    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    @Override
    public void put(String sessionId, SessionContext ctx, long ttlSeconds) {
        Entry e = new Entry();
        e.ctx = ctx;
        e.expireAt = Instant.now().plusSeconds(ttlSeconds);

        store.put(sessionId, e);
    }

    @Override
    public SessionContext require(Long userId, String sessionId) {
        Entry e = store.get(sessionId);

        if (e == null){
            throw new SessionNotFoundException();
        };
        if (Instant.now().isAfter(e.expireAt)){
            store.remove(sessionId);
            throw new SessionExpiredException();
        }
        if (!e.ctx.userId().equals(userId)){
            throw new SessionOwnerMismatchException();
        }

        return e.ctx;
    }

    @Override
    public SessionContext get(String sessionId) {
        Entry e = store.get(sessionId);

        if (e == null) return null;
        if (Instant.now().isAfter(e.expireAt)){
            store.remove(sessionId);
            return null; // get은 만료를 null로 취급
        }
        return e.ctx;
    }

    @Override
    public void remove(String sessionId) {
        store.remove(sessionId);
    }

    public static String newSessionId(){
        return UUID.randomUUID().toString();
    }
}
