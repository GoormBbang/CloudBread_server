package com.cloudbread.domain.mealplan.domain.repository;

import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
}
