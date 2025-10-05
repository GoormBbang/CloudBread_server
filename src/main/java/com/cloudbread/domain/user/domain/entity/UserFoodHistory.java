package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.user.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_food_history")
public class UserFoodHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) // BREAKFAST, LUNCH, DINNER 으로 저장됨
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    private int intakePercent; // 0~100 %

    private LocalDateTime createdAt;

    // FK → food.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_analysis_id")
    private Food food;
}
