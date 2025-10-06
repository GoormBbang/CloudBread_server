package com.cloudbread.domain.mealplan.domain.entity;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.user.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_plan_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealPlanItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ AI 추천 식단 id (meal_plan_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    // ✅ 음식 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    // ✅ 식사 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    // ✅ 한 음식당 칼로리
    @Column(name = "est_calories")
    private Integer estCalories;

    // ✅ 음식 기준 양
    @Column(name = "portion_label")
    private String portionLabel;

    // ✅ 음식 카테고리
    @Column(name = "category")
    private String category;

    @Builder
    public MealPlanItem(MealPlan mealPlan, Food food, MealType mealType,
                        Integer estCalories, String portionLabel, String category) {
        this.mealPlan = mealPlan;
        this.food = food;
        this.mealType = mealType;
        this.estCalories = estCalories;
        this.portionLabel = portionLabel;
        this.category = category;
    }
}
