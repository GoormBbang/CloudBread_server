//package com.cloudbread.domain.mealplan.domain.entity;
//
//import com.cloudbread.domain.food.domain.entity.Food;
//import com.cloudbread.domain.user.domain.enums.MealType;
//import jakarta.persistence.*;
//import lombok.*;

//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class MealPlanItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // ✅ 연관관계 주인 (외래키 가짐)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "meal_plan_id")
//    @Setter
//    private MealPlan mealPlan;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "food_id")
//    private Food food;
//
//    @Enumerated(EnumType.STRING)
//    private MealType mealType;
//
//    private Integer estCalories;
//
//    private String portionLabel;
//
//    private String category;
//}
package com.cloudbread.domain.mealplan.domain.entity;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.user.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MealPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 주인 (FK 소유)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    @Setter // MealPlan.addMealPlanItem()에서 사용
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    private String foodName;
    private String portionLabel;
    private Integer estCalories;
    private String foodCategory;
}
