package com.cloudbread.domain.notifiaction.domain;


import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class Notification  {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private NotificationType type; // 알림 타입 enum

    @Column(nullable=false)
    private String title; // 알림 제목

    @Column(nullable=false, columnDefinition="TEXT")
    private String body; // 알림 본문

    @Convert(converter = TagListJsonConverter.class)
    @Column(columnDefinition="TEXT")
    private List<String> tags;

    private String deepLink;                  // null 허용


    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    private Instant deletedAt;                // 소프트 삭제

    public static Notification create(User u, NotificationType type, String title, String body,
                                      List<String> tags, String deepLink) {
        Notification n = new Notification();
        n.user = u;
        n.type = type;
        n.title = title;
        n.body = body;
        n.tags = tags;
        n.deepLink = deepLink;
        return n;
    }
    private static String toJson(List<String> tags) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(tags); }
        catch (Exception e) { return "[]"; }
    }
}