package com.cloudbread.domain.photo_analyses.application.event;

import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 서버 -> 프론트로 이벤트 푸시
@Component
@Slf4j
public class PhotoAnalysisSseManager {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 이때 id는 photoAnalysisId임

    public SseEmitter subscribe(Long id) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(e -> emitters.remove(id));

        return emitter;
    }

    public void sendStatus(Long id, PhotoAnalysisStatus status) {
        SseEmitter em = emitters.get(id);

        if (em == null) return;
        try {
            em.send(SseEmitter.event().name("status")
                    .data(Map.of("status", status.name())));
        } catch (IOException e) {
            emitters.remove(id);
        }
    }

    public void sendCandidates(Long id, Object payload) {
        SseEmitter em = emitters.get(id);

        if (em == null) return;
        try {
            em.send(SseEmitter.event().name("candidates").data(payload));
        } catch (IOException e) {
            emitters.remove(id);
        }
    }
}
