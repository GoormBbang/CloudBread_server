package com.cloudbread.domain.alert.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertPreference {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "meal_reco_enabled", nullable = false)
    private boolean mealRecoEnabled = true;

    @Column(name = "nutrient_alert_enabled", nullable = false)
    private boolean nutrientAlertEnabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePreferences(boolean mealRecoEnabled, boolean nutrientAlertEnabled) {
        this.mealRecoEnabled = mealRecoEnabled;
        this.nutrientAlertEnabled = nutrientAlertEnabled;
        this.updatedAt = LocalDateTime.now();
    }
}
