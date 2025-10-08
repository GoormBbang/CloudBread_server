package com.cloudbread.domain.notifiaction.dto;

import com.cloudbread.domain.notifiaction.domain.Notification;

public class NotificationMapper {
    public static NotificationDtos.ListItem toListItem(Notification n) {
        return NotificationDtos.ListItem.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .tags(n.getTags())
                .deepLink(n.getDeepLink())
                .createdAt(n.getCreatedAt())
                .build();
    }
    public static NotificationDtos.StreamPayload toStreamPayload(Notification n) {
        return NotificationDtos.StreamPayload.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .tags(n.getTags())
                .deepLink(n.getDeepLink())
                .sentAt(n.getSentAt() != null ? n.getSentAt() : n.getCreatedAt())
                .build();
    }
}
