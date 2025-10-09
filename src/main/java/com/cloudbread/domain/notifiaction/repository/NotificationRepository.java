package com.cloudbread.domain.notifiaction.repository;

import com.cloudbread.domain.notifiaction.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("select n from Notification n " +
            "where n.user.id = :userId and n.deletedAt is null and n.id > :lastId " +
            "order by n.id asc")
    List<Notification> findReplay(Long userId, Long lastId);

    @Query("select n from Notification n " +
            "where n.user.id = :userId and n.deletedAt is null " +
            "order by n.id desc")
    List<Notification> findList(Long userId, Pageable pageable);

    @Modifying
    @Query("update Notification n set n.deletedAt = :now where n.user.id = :userId and n.deletedAt is null")
    int softDeleteAll(Long userId, Instant now);
}
