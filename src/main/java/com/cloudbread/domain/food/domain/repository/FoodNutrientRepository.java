package com.cloudbread.domain.food.domain.repository;

import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodNutrientRepository extends JpaRepository<FoodNutrient, Long> {
    Optional<FoodNutrient> findByFoodIdAndNutrientId(Long foodId, Long nutrientId);
    List<FoodNutrient> findByFoodId(Long foodId);

    // 영양 분석용
    @Query("""
        SELECT fn
        FROM FoodNutrient fn
        JOIN FETCH fn.nutrient n
        JOIN FETCH fn.food f
        WHERE f.id IN :foodIds
        AND UPPER(n.name) IN :nutrientNames
        """)
    List<FoodNutrient> findNutrientsByFoodIdsAndNames(
            @Param("foodIds") List<Long> foodIds,
            @Param("nutrientNames") List<String> nutrientNames
    );

    //캘린더 상세조회용
    @Query("""
    SELECT fn
    FROM FoodNutrient fn
    JOIN FETCH fn.nutrient n
    WHERE fn.food.id IN :foodIds
    """)
    List<FoodNutrient> findByFoodIdsWithNutrient(@Param("foodIds") List<Long> foodIds);


}
