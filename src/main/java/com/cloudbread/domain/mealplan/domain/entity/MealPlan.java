package com.cloudbread.domain.mealplan.domain.entity;

import com.cloudbread.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate planDate;

    private String reasonDesc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ Cascade + orphanRemoval + Builder.Default 필수
    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealPlanItem> mealPlanItems = new ArrayList<>();

    // ✅ 양방향 편의 메서드
    public void addMealPlanItem(MealPlanItem item) {
        mealPlanItems.add(item);
        item.setMealPlan(this);
    }
}
