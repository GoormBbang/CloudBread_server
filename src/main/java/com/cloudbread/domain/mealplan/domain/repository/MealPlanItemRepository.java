package com.cloudbread.domain.mealplan.domain.repository;

import com.cloudbread.domain.mealplan.domain.entity.MealPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanItemRepository extends JpaRepository<MealPlanItem, Long> { }
