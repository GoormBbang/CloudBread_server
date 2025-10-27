package com.cloudbread.domain.notifiaction.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  스프링에서 SSE를 효율적으로 관리하기 위한 저장소(Repository) 역할
 *
 *  - store : userID별로, 여러 개의 SSE 연결을 저장하기 위한 역할
 *     Long : 사용자 식별하기 위한 고유 ID
 *     ConcurrentHasHMap : 여러 스레드가 동시에 접근해도, 안전하게 데이터를 읽고 쓸 수 있는 스레드 안전 맵
 *
 *  - add : 특정 userID에 대한 새로운 SSE 연결 추가
 *  - remove : 특정 userID에 연결된 SSE 연결 제거
 *  - list : 특정 userID에 연결된 모든 SseEmitter 리스트 반환
 *  - allUserIds : 현재 store에 연결된 모든 사용자 ID를 반환
 */
@Component
public class SseEmitterRepository {
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> store = new ConcurrentHashMap<>();

    public void add(Long userId, SseEmitter emitter) {
        store.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }
    public void remove(Long userId, SseEmitter emitter) {
        var list = store.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) store.remove(userId);
        }
    }
    public List<SseEmitter> list(Long userId) {
        return store.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }
    public Set<Long> allUserIds() { return store.keySet(); }
}
