package com.cloudbread.domain.food.domain.repository;

import com.cloudbread.domain.food.domain.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByExternalId(String externalId);
}
