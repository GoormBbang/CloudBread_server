package com.cloudbread.domain.notifiaction.repository;

import com.cloudbread.domain.notifiaction.domain.Notification;
import com.cloudbread.domain.notifiaction.domain.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
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

    @Query("""
      select count(n) > 0
      from Notification n
      where n.user.id = :userId
        and n.type = :type
        and DATE(n.createdAt) = :date
    """)
    boolean existsByUserIdAndTypeOnDate(Long userId, NotificationType type, LocalDate date);
}
