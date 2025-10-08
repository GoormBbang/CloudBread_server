package com.cloudbread.domain.notifiaction.application;

import com.cloudbread.domain.notifiaction.dto.NotificationMapper;
import com.cloudbread.domain.notifiaction.repository.NotificationRepository;
import com.cloudbread.domain.notifiaction.sse.SseEmitterRepository;
import com.cloudbread.domain.notifiaction.sse.SseSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 알림 구독 + replay
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSseService {
    private final SseEmitterRepository emitterRepo;
    private final NotificationRepository notificationRepository;
    private final SseSender sseSender;
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long userId, String lastEventId) {
        // emitter 생성/등록 (1시간 타임아웃)
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L);
        emitterRepo.add(userId, emitter);

        // 정리 콜백 (SSE연결 무한정 연결 X, 때에 따라 끊는 것이 안정적인 연결)
        emitter.onCompletion(() -> emitterRepo.remove(userId, emitter)); // 클라이언트가 연결을 정상적으로 종료했을 때 호출
        emitter.onTimeout(() -> emitterRepo.remove(userId, emitter)); // 설정된 타임아웃시간동안 아무런 데이터전송이 없어, 연결이 끊어졌을 때 호출
        emitter.onError(e -> emitterRepo.remove(userId, emitter)); // 네트워크 오류 등 예기치 않은 문제로 연결 끊어졌을 때

        try {
            // 브라우저 재연결 간격 힌트 (브라우저에게 만약 연결이 끊어지면, 10초 후에 다시 연결을 시도하라는 힌트)
            emitter.send(SseEmitter.event().reconnectTime(10_000));

            // Last-Event-ID 이후 미수신 알림 replay (놓친 이벤트 다시 보내기)
            if (lastEventId != null && !lastEventId.isBlank()) {
                long lastId = Long.parseLong(lastEventId.trim());
                var list = notificationRepository.findReplay(userId, lastId);
                log.info("[SSE][REPLAY] userId={}, fromId>( {} ), count={}", userId, lastId, list.size());
                list.forEach(n -> {
                    log.info("[SSE][REPLAY->SEND] userId={}, replayId={}", userId, n.getId());
                    sseSender.send(userId, "notification", String.valueOf(n.getId()),
                            NotificationMapper.toStreamPayload(n));
                });
//                notificationRepository.findReplay(userId, lastId).forEach(n ->
//                        sseSender.send(userId, "notification", String.valueOf(n.getId()), NotificationMapper.toStreamPayload(n))
//                );
            } else {
                log.info("[SSE][REPLAY] userId={}, no Last-Event-ID (fresh connection)", userId);
            }
        } catch (Exception e) {
            log.warn("[SSE] initial send failed", e);
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
