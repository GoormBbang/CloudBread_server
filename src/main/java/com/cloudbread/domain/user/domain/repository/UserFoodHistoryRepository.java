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

    /**
     * 특정 사용자의 기간별 식단 기록 조회 (Food 정보 함께 Fetch)
     * N+1 문제 해결을 위한 Join Fetch
     */
    @Query("SELECT ufh FROM UserFoodHistory ufh " +
            "JOIN FETCH ufh.food " +
            "WHERE ufh.user.id = :userId " +
            "AND ufh.createdAt >= :startDate " +
            "AND ufh.createdAt < :endDate")
    List<UserFoodHistory> findByUserIdAndCreatedAtBetweenWithFood(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    //영양요약
    @Query("SELECT h FROM UserFoodHistory h JOIN FETCH h.food f WHERE h.user.id = :userId AND DATE(h.createdAt) = :date")
    List<UserFoodHistory> findByUserIdAndDateWithFood(@Param("userId") Long userId, @Param("date") LocalDate date);


    // ✅ 오늘 먹은 음식 조회 (API: /api/users/me/food-history/today)
    @Query("""
    SELECT ufh.mealType, f.id, f.name, f.calories, f.imageUrl
    FROM UserFoodHistory ufh
    JOIN ufh.food f
    WHERE ufh.user.id = :userId
    AND DATE(ufh.createdAt) = :date
    ORDER BY FIELD(ufh.mealType, 'BREAKFAST', 'LUNCH', 'DINNER'), ufh.createdAt
    """)
    List<Object[]> findTodayFoods(@Param("userId") Long userId, @Param("date") LocalDate date);
}

