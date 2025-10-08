package com.cloudbread.domain.notifiaction.domain;


import com.cloudbread.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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

    @Column(nullable=false)
    private Instant createdAt;

    private Instant sentAt;                   // SSE 최초 전송 시각(없으면 createdAt 보여줘도 OK)
    private Instant deletedAt;                // 소프트 삭제
}