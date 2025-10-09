package com.cloudbread.domain.notifiaction.application;

import com.cloudbread.domain.notifiaction.dto.NotificationDtos;
import com.cloudbread.domain.notifiaction.dto.NotificationMapper;
import com.cloudbread.domain.notifiaction.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

// 조회/삭제
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationDtos.ListItem> list(Long userId, int limit) {
        return notificationRepository.findList(userId, PageRequest.of(0, Math.max(1, Math.min(limit, 100))))
                .stream().map(NotificationMapper::toListItem).toList();
    }

    @Transactional
    public int softDeleteAll(Long userId) {
        return notificationRepository.softDeleteAll(userId, Instant.now());
    }
}
