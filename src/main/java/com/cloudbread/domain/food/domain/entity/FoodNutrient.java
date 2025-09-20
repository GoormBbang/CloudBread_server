package com.cloudbread.domain.food.domain.entity;


import com.cloudbread.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "food_nutrients")
@Getter
public class FoodNutrient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne
    @JoinColumn(name = "nutrient_id", nullable = false)
    private Nutrient nutrient;

    @Column(nullable = false)
    private BigDecimal value; // 52.3

    @Builder
    public FoodNutrient(Food food, Nutrient nutrient, BigDecimal value) {
        this.food = food;
        this.nutrient = nutrient;
        this.value = value;
    }

    public boolean updateValueIfChanged(BigDecimal newValue) {
        if (newValue == null) return false;
        if (this.value == null || this.value.compareTo(newValue) != 0) {
            this.value = newValue;
            return true;
        }
        return false;
    }
}