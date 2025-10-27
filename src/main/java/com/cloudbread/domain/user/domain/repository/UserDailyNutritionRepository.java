package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.nutrition.domain.entity.UserDailyNutrition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserDailyNutritionRepository extends JpaRepository<UserDailyNutrition, Long> {
    List<UserDailyNutrition> findByUserIdAndDate(Long userId, LocalDate date);
}

