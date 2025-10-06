package com.cloudbread.domain.mealplan.domain.repository;

import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


}
