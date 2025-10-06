package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import com.cloudbread.domain.user.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

    private LocalDateTime createdAt; // 먹은 시간

    // FK → food.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_analysis_id")
    private PhotoAnalysis photoAnalysis; // 선택

    public static UserFoodHistory of(User user, Food food, PhotoAnalysis pa,
                                 MealType mealType, int intakePercent, LocalDateTime createdAt) {
        UserFoodHistory fh = new UserFoodHistory();
        fh.user = user;
        fh.food = food;
        fh.photoAnalysis = pa;
        fh.mealType = mealType;
        fh.intakePercent = intakePercent;
        fh.createdAt = createdAt;
        return fh;
    }
}

