package com.cloudbread.domain.nutrition.domain.entity;

import com.cloudbread.domain.food.domain.enums.NutrientType;
import com.cloudbread.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_daily_nutrition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDailyNutrition {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NutrientType nutrient; // CARBS, PROTEINS, FATS ...

    @Column(nullable = false)
    private BigDecimal actual;

    @Column(nullable = false)
    private BigDecimal recommended;

    @Column(nullable = false, length = 8)
    private String unit;
}
