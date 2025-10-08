package com.cloudbread.domain.notifiaction.scheduler;

import com.cloudbread.domain.notifiaction.sse.SseEmitterRepository;
import com.cloudbread.domain.notifiaction.sse.SseSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 15,000밀리초, 즉 15초마다 SSE 연결을 유지하기 위해 보내는 하트비트
 */
@Component
@RequiredArgsConstructor
public class NotificationKeepaliveScheduler {
    private final SseEmitterRepository repo;
    private final SseSender sender;

    @Scheduled(fixedRate = 15_000)
    public void heartbeat() {
        for (Long userId : repo.allUserIds()) {
            sender.send(userId, "keepalive", null, "ok");
        }
    }
}
