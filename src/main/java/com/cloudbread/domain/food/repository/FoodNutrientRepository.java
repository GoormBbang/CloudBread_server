package com.cloudbread.domain.food.repository;

import com.cloudbread.domain.food.domain.FoodNutrient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodNutrientRepository extends JpaRepository<FoodNutrient, Long> {
    Optional<FoodNutrient> findByFoodIdAndNutrientId(Long foodId, Long nutrientId);
}
