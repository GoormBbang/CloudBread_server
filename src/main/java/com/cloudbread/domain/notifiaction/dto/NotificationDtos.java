package com.cloudbread.domain.notifiaction.dto;

import com.cloudbread.domain.notifiaction.domain.NotificationType;
import lombok.*;

import java.time.Instant;
import java.util.List;

// ListItem, DeleteAllRes, StreamPayload
public class NotificationDtos {
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ListItem { // 알림 이벤트 dto
        private Long id;
        private NotificationType type;
        private String title;
        private String body;
        private List<String> tags;
        private String deepLink;
        private Instant createdAt;  // 목록에서 상대시간 계산용
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DeleteAllRes {
        private int deletedCount; // 삭제된 알림 개수
    }

    // SSE payload (data:)
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StreamPayload {
        private Long id;
        private NotificationType type;
        private String title;
        private String body;
        private List<String> tags;
        private String deepLink;
        private Instant sentAt; // 없으면 createdAt으로 채워 전송
    }
}
