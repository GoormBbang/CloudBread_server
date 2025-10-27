package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.food_history.dto.DayMealRecord;
import com.cloudbread.domain.notifiaction.application.dto.NutrientTotal;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.cloudbread.domain.user.domain.enums.MealType;
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


    // 오늘 먹은 음식 조회 (API: /api/users/me/food-history/today)
    @Query("""
    SELECT ufh.mealType, f.id, f.name, f.calories, f.imageUrl
    FROM UserFoodHistory ufh
    JOIN ufh.food f
    WHERE ufh.user.id = :userId
    AND ufh.createdAt BETWEEN :startOfDay AND :endOfDay
    ORDER BY FIELD(ufh.mealType, 'BREAKFAST', 'LUNCH', 'DINNER'), ufh.createdAt
    """)
    List<Object[]> findTodayFoods(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );


    /**
     *
     * SUM( fn.value * (ufh.intake_percent / 100.0) )
     * - fn.value : 해당 음식의 영양소 1회 기준량
     * - intake_percent : 실제로 먹은 비율 (1~100)
     * - 예: 엽산 700μg짜리 음식을 **50%**만 먹었으면 기여량은 350μg.
     *
     * - 그룹 기준 : nutrient.name
     * → 결과를 Map<String, Double>로 변환해서 totals로 사용.
     */
    @Query(value = """
    SELECT n.name AS nutrientKey,
           SUM(COALESCE(fn.value, 0) * (ufh.intake_percent / 100.0)) AS totalAmount
    FROM user_food_history ufh
    JOIN foods f                ON f.id = ufh.food_id
    JOIN food_nutrients fn      ON fn.food_id = f.id
    JOIN nutrients n            ON n.id = fn.nutrient_id
    WHERE ufh.user_id = :userId
      AND ufh.created_at >= :start
      AND ufh.created_at <  :end
    GROUP BY n.name
""", nativeQuery = true)
    List<Object[]> sumDailyNutrientsRaw(
            @Param("userId") Long userId,
            @Param("start")  LocalDateTime startInclusive,
            @Param("end")    LocalDateTime endExclusive
    );

//    // 해당 날짜(하루) 범위에 특정 끼니 기록 존재 여부
//    boolean existsByUser_IdAndMealTypeAndCreatedAtBetween(
//            Long userId,
//            MealType mealType,
//            LocalDateTime startInclusive,
//            LocalDateTime endExclusive
//    );

    @Query("""
        select distinct h.mealType
        from UserFoodHistory h
        where h.user.id = :userId
          and h.createdAt >= :start
          and h.createdAt <  :end
        """)
    List<MealType> findDistinctMealsOfDay(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );
}

