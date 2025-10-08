package com.cloudbread.domain.notifiaction.domain;

public enum NotificationType {
    NUTRIENT_DEFICIT, // 영양소 부족 알림 (권장량 90% 미만 Top 2 ~ 3개 )
    NUTRIENT_GOAL_ACHIEVED, // 목표 달성 (임신주성분인, 엽산,칼슘,철분 중 달성 >= 100% 묶어서 1건)
    MEAL_LOG_MISSED // 식단 기록 누락 (아,점,저 기록 누락)
}
