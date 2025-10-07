package com.cloudbread.domain.chat.main.application.session_store;

import com.cloudbread.domain.chat.nutrients.exception.SessionExpiredException;
import com.cloudbread.domain.chat.nutrients.exception.SessionNotFoundException;
import com.cloudbread.domain.chat.nutrients.exception.SessionOwnerMismatchException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiInMemorySessionStore implements AiSessionStore {
    private static class Entry {
        AiSessionContext ctx;
        Instant expireAt;
    }
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void put(String sessionId, AiSessionContext ctx, long ttlSeconds) {
        Entry e = new Entry();

        e.ctx = ctx;
        e.expireAt = Instant.now().plusSeconds(ttlSeconds);
        store.put(sessionId, e);
    }

    @Override
    public AiSessionContext require(Long userId, String sessionId) {
        Entry e = store.get(sessionId);

        if (e == null) throw new SessionNotFoundException();
        if (Instant.now().isAfter(e.expireAt)) {
            store.remove(sessionId);
            throw new SessionExpiredException();
        }
        if (!e.ctx.userId().equals(userId)) throw new SessionOwnerMismatchException();

        return e.ctx;
    }

    @Override
    public AiSessionContext get(String sessionId) {
        Entry e = store.get(sessionId);

        if (e == null) return null;
        if (Instant.now().isAfter(e.expireAt)) {
            store.remove(sessionId);
            return null;
        }
        return e.ctx;

    }

    @Override
    public void remove(String sessionId) {
        store.remove(sessionId);
    }


    @Override
    public void touch(String sessionId, long ttlSeconds) {
        Entry e = store.get(sessionId);
        if (e != null) e.expireAt = Instant.now().plusSeconds(ttlSeconds);
    }

    @Override
    public void updateTopic(String sessionId, String topic) {
        Entry e = store.get(sessionId);
        if (e != null) {
            e.ctx = new AiSessionContext(e.ctx.userId(), topic, e.ctx.userProfile(), e.ctx.createdAt());
        }
    }
}
