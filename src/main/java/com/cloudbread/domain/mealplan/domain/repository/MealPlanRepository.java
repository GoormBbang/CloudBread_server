package com.cloudbread.domain.mealplan.domain.repository;

import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @Query("""
    SELECT DISTINCT mp
    FROM MealPlan mp
    JOIN FETCH mp.mealPlanItems i
    JOIN FETCH i.food f
    WHERE mp.id = :planId
""")
    Optional<MealPlan> findByIdWithItemsAndFoods(@Param("planId") Long planId);

    @Query("""
           select distinct mp
           from MealPlan mp
           left join fetch mp.mealPlanItems i
           left join fetch i.food f
           where mp.user.id = :userId
             and mp.planDate = :planDate
           """)
    Optional<MealPlan> findOneWithItemsByUserIdAndPlanDate(
            @Param("userId") Long userId,
            @Param("planDate") LocalDate planDate
    );

    @Query("""
    select distinct mp
    from MealPlan mp
    left join fetch mp.mealPlanItems i
    left join fetch i.food f
    where mp.user.id = :userId
      and mp.planDate = :planDate
      and mp.id = (
        select max(m2.id)
        from MealPlan m2
        where m2.user.id = :userId
          and m2.planDate = :planDate
      )
""")
    Optional<MealPlan> findLatestWithItemsByUserIdAndPlanDate(
            @Param("userId") Long userId,
            @Param("planDate") LocalDate planDate
    );


}
