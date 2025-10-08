package com.cloudbread.domain.notifiaction.sse;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 스프링에서 SSE를 이용해, 클라이언트에게 실시간 데이터 전송 역할
 * SseEmitterRepository를 사용해, 특정 user에게 메시지(이벤트)를 실제로 보내는 핵심 로직
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseSender {
    private final SseEmitterRepository repo;
    private final ObjectMapper om;

    public void send(Long userId, String eventName, String idOrNull, Object payload) {
        var list = repo.list(userId); // 수신 대상 확인
        if (list.isEmpty()) return;

        String json; // 전송할 데이터인 payload 객체를 ObjectMapper를 이용해 JSON문자열로 직렬화
        try { json = om.writeValueAsString(payload); }
        catch (Exception e) { log.warn("[SSE] payload json serialize fail", e); return; }

        for (SseEmitter emitter : list) { // 가져온 emitter 리스트를 순회하며 각 클라이언트에게 데이터 send
            try {
                var ev = SseEmitter.event().name(eventName).data(json);
                if (idOrNull != null) ev.id(idOrNull); // 이벤트의 고유ID
                emitter.send(ev);
            } catch (Exception e) {
                log.warn("[SSE] send failed -> remove emitter (userId={})", userId, e);
                emitter.completeWithError(e); // 데이터 보내던 중 에러나면, 연결 유효 X -> emitter 연결 종료 후
                repo.remove(userId, emitter); // 저장소에서 해당 emitter 제거
            }
        }
    }
}