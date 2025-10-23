package com.cloudbread.domain.notifiaction.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.notifiaction.application.NotificationTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/***
 *  TODO : 배치,스케쥴러가 각 서비스를 시간대별로 호출하게 둘 것
 *
 *  역할 : 배치/운영툴이 호출 -> Service가 생성/저장 -> pushIfConnected
 */
@RestController
@RequestMapping("/api/internal/notifications/generate")
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationGenerateController {

    private final NotificationTriggerService notificationTriggerService;

    /** 21:00 일일 요약(부족/목표) */
    @PostMapping("/daily")
    public void generateDaily(@RequestBody DailyReq req, @AuthenticationPrincipal CustomOAuth2User principal) {
        notificationTriggerService.generateDaily(principal.getUserId(), req.date, req.sendNow);
    }


    /** 10:30/14:30/20:30 끼니 누락 */
//    @PostMapping("/meal-missed")
//    public void generateMealMissed(@RequestBody MissedReq req, @AuthenticationPrincipal CustomOAuth2User principal) {
//        notificationTriggerService.generateMealMissedFake(principal.getUserId(), req.date, req.meal, req.sendNow);
//    }

    @lombok.Data static class DailyReq {
        public java.time.LocalDate date;     // null이면 오늘
        public boolean sendNow = true;
    }
    @lombok.Data static class MissedReq {
        public java.time.LocalDate date;
        public com.cloudbread.domain.user.domain.enums.MealType meal; // BREAKFAST/LUNCH/DINNER
        public boolean sendNow = true;
    }
}

