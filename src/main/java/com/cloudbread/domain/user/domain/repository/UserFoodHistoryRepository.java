package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.food_history.dto.DayMealRecord;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFoodHistoryRepository extends JpaRepository<UserFoodHistory, Long> {
    @Query("SELECT f FROM UserFoodHistory f WHERE f.user.id = :userId AND f.createdAt > :fromDate ORDER BY f.createdAt DESC")
    List<UserFoodHistory> findRecentByUserId(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate);

    // ✅ 오늘의 영양 분석용 (createdAt 기준)
    List<UserFoodHistory> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("""
    SELECT new com.cloudbread.domain.food_history.dto.DayMealRecord(
        DAY(ufh.createdAt),
        ufh.mealType
    )
    FROM UserFoodHistory ufh
    WHERE ufh.user.id = :userId
    AND ufh.createdAt BETWEEN :startDate AND :endDate
    """)
    List<DayMealRecord> findMealRecordsByMonth(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}

