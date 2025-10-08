package com.cloudbread.domain.notifiaction.api.test;

import com.cloudbread.domain.notifiaction.sse.SseEmitterRepository;
import com.cloudbread.domain.notifiaction.sse.SseSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

// Dev 전용 컨트롤러
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Slf4j
public class DevNotifyController {
    private final SseSender sender;
    private final SseEmitterRepository repo;

    @PostMapping("/notify")
    public void send(@RequestParam Long userId,
                     @RequestParam(defaultValue="notification") String event,
                     @RequestParam(defaultValue="0") Long id) {


        var payload = Map.of(
                "id", id,
                "type", "MEAL_LOG_MISSED",
                "title", "테스트 알림",
                "body", "점심 기록 안함",
                "tags", List.of("LUNCH"),
                "deepLink", "app://food/add?meal=LUNCH",
                "sentAt", java.time.OffsetDateTime.now().toString()
        );
        sender.send(userId, event, String.valueOf(id), payload);
    }
}

