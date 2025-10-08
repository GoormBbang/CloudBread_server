package com.cloudbread.domain.notifiaction.application;

import com.cloudbread.domain.notifiaction.domain.Notification;
import com.cloudbread.domain.notifiaction.dto.NotificationMapper;
import com.cloudbread.domain.notifiaction.repository.NotificationRepository;
import com.cloudbread.domain.notifiaction.sse.SseEmitterRepository;
import com.cloudbread.domain.notifiaction.sse.SseSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * 알림 PUSH : "이미 만들어진 알림"을, SSE와 연결되어 있으면 즉시 SSE로 기록 + send_at 기록
 *
 * DB에 만들어진 알림을, 연결된 SSE가 있다면 즉시 push하고 send_at 찍기
 * 연결 없으면 아무것도 안하고 DB만 (=다음 재연결 때 replay 대상)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushService {
    private final SseEmitterRepository emitterRepo;
    private final SseSender sseSender;
    private final NotificationRepository notificationRepository;

    /** 연결돼 있으면 즉시 push + send_at 업데이트 */
    @Transactional
    public void pushIfConnected(Notification n) {
        var emitters = emitterRepo.list(n.getUser().getId());
        if (emitters.isEmpty()) {
            log.info("[PUSH] no active SSE. userId={}, notifId={}", n.getUser().getId(), n.getId());
            return;
        }
        sseSender.send(
                n.getUser().getId(),
                "notification",
                String.valueOf(n.getId()),
                NotificationMapper.toStreamPayload(n)
        );
        n.setSentAt(Instant.now());
        notificationRepository.save(n); // send_at 반영
        log.info("[PUSH] pushed. userId={}, notifId={}", n.getUser().getId(), n.getId());
    }
}
