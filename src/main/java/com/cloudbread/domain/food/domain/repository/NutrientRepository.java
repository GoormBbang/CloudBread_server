package com.cloudbread.domain.food.domain.repository;

import com.cloudbread.domain.food.domain.entity.Nutrient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutrientRepository extends JpaRepository<Nutrient, Long> {
    Optional<Nutrient> findByName(String name);
}
