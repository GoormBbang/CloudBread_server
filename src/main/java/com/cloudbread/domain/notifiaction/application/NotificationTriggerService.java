package com.cloudbread.domain.notifiaction.application;

import com.cloudbread.domain.notifiaction.api.InternalNotificationGenerateController;
import com.cloudbread.domain.notifiaction.application.util.PregnancyStageUtil;
import com.cloudbread.domain.notifiaction.domain.Notification;
import com.cloudbread.domain.notifiaction.domain.NotificationType;
import com.cloudbread.domain.notifiaction.repository.NotificationRepository;
import com.cloudbread.domain.nutrition.constant.RecommendedNutrientConstants;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.enums.MealType;
import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.cloudbread.domain.user.domain.enums.MealType.*;

/**
 * - 모든 활성유저(또는 대상유저)를 훑는다
 * - 당일 섭취 합계 구해서 부족 top2~3, 엽산/칼슘/철분 달성 판단 -> Notification 생성 & 저장
 * - sendNow=true면 pushIfConnected 호출
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPushService notificationPushService;
    private final UserFoodHistoryRepository userFoodHistoryRepository;

    private User getTargetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    }

    /** [FAKE] 21:00 일일요약: 부족 1건 + 목표 1건을 하드코딩 생성 */
    @Transactional
    public void generateDailyFake(Long userId, LocalDate dateOrNull, boolean sendNow) {
        User user = getTargetUser(userId);

        // 1) 영양소 부족(그럴듯한 태그 2개)
        Notification deficit = Notification.create(
                user,
                NotificationType.NUTRIENT_DEFICIT,
                "영양소 부족 알림",
                "오늘 엽산과 칼슘이 권장량보다 부족해요.",
                List.of("FOLIC_ACID", "CALCIUM"),
                null // 정보성이라 딥링크 없음
        );
        notificationRepository.save(deficit);
        if (sendNow) notificationPushService.pushIfConnected(deficit);

        // 2) 목표 달성(중요 3종 중 2개 달성했다고 가정)
        Notification goal = Notification.create(
                user,
                NotificationType.NUTRIENT_GOAL_ACHIEVED,
                "목표 달성",
                "오늘 엽산·철분 목표를 달성했어요! 잘하셨어요 👏",
                List.of("FOLIC_ACID", "IRON"),
                null
        );
        notificationRepository.save(goal);
        if (sendNow) notificationPushService.pushIfConnected(goal);

        log.info("[FAKE-DAILY] userId={}, created deficit(id={}), goal(id={})",
                userId, deficit.getId(), goal.getId());
    }

    /** [FAKE] 끼니 누락 1건을 하드코딩 생성 */
    @Transactional
    public void generateMealMissedFake(Long userId, LocalDate date, MealType meal, boolean sendNow) {
        User user = getTargetUser(userId);

        String body = switch (meal) {
            case BREAKFAST -> "아침 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
            case LUNCH     -> "점심 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
            case DINNER    -> "저녁 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
            default        -> "식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
        };

      //  String deepLink = "app://food/add?meal=" + meal.name() + "&date=" + date;

        Notification missed = Notification.create(
                user,
                NotificationType.MEAL_LOG_MISSED,
                "식단 기록 누락",
                body,
                List.of(meal.name()),     // ["LUNCH"] 같은 태그
                null                  // 눌렀을 때 식단 추가로 이동
        );
        notificationRepository.save(missed);
        if (sendNow) notificationPushService.pushIfConnected(missed);

        log.info("[FAKE-MISSED] userId={}, meal={}, id={}", userId, meal, missed.getId());
    }


    // TODO : 아래 실제 구현체
//    // ====== 21:00 일일 요약(부족/목표) ======
//    @Transactional
//    public void generateDaily(LocalDate dateOrNull, boolean sendNow) {
//        LocalDate date = (dateOrNull != null) ? dateOrNull : LocalDate.now();
//        log.info("[DAILY] generate for {}", date);
//
//        List<User> users = userRepository.findAllActivated(); // 활성 유저
//        for (User u : users) {
//            var stage = PregnancyStageUtil.stageOf(u.getDueDate(), date);
//            Map<String, Double> totals = loadDailyTotals(u.getId(), date); // ← TODO 실제 구현
//
//            // 1) NUTRIENT_DEFICIT: 권장량 90% 미만 Top2~3
//            var deficitTags = pickDeficitTags(totals, stage, 0.90, 3);
//            if (!deficitTags.isEmpty()) {
//                var n1 = Notification.create(
//                        u, NotificationType.NUTRIENT_DEFICIT,
//                        "영양소 부족 알림",
//                        makeDeficitBody(deficitTags),
//                        deficitTags, null
//                );
//                notificationRepository.save(n1);
//                if (sendNow) notificationPushService.pushIfConnected(n1);
//            }
//
//            // 2) NUTRIENT_GOAL_ACHIEVED: 엽산/칼슘/철분 달성 ≥100%
//            var goalTags = pickAchievedTags(totals, stage, List.of("FOLIC_ACID","CALCIUM","IRON"));
//            if (!goalTags.isEmpty()) {
//                var n2 = Notification.create(
//                        u, NotificationType.NUTRIENT_GOAL_ACHIEVED,
//                        "목표 달성",
//                        makeGoalBody(goalTags),
//                        goalTags, null
//                );
//                notificationRepository.save(n2);
//                if (sendNow) notificationPushService.pushIfConnected(n2);
//            }
//        }
//    }
//
//    // ====== 끼니 누락 ======
//    @Transactional
//    public void generateMealMissed(LocalDate date, MealType meal, boolean sendNow) {
//        List<User> users = userRepository.findAllActivated();
//        for (User u : users) {
////            boolean exists = userFoodHistoryRepository.existsByUserIdAndDateAndMeal(u.getId(), date, meal); // ← TODO
////            boolean already = notificationRepository.existsMealMissed(u.getId(), date, meal);                // ← TODO
////            if (exists || already) continue;
//
//   //         String deepLink = buildMealDeepLink(meal, date);
//            var n = Notification.create(
//                    u, NotificationType.MEAL_LOG_MISSED,
//                    "식단 기록 누락",
//                    mealKorean(meal) + " 식사를 기록하지 않았어요. 기록해주세요.",
//                    List.of(meal.name()), /*deepLink*/ null
//            );
//            notificationRepository.save(n);
//            if (sendNow) notificationPushService.pushIfConnected(n);
//        }
//    }
//
//    // ================= helpers =================
//
//    private Map<String, Double> loadDailyTotals(Long userId, LocalDate date) {
//        // TODO: user_food_history + food_nutrients 조인으로 당일 섭취량 합계 맵 반환
//        // key: "FOLIC_ACID", "CALCIUM", ...
//        return Map.of(); // 임시
//    }
//
//    private List<String> pickDeficitTags(Map<String, Double> totals, PregnancyStageUtil.Stage stage,
//                                         double threshold, int topN) {
//        // RecommendedNutrientConstants 사용
//        List<Map.Entry<String, Double>> under = new ArrayList<>();
//        for (var e : RecommendedNutrientConstants.RECOMMENDED_NUTRIENTS.entrySet()) {
//            String nutrient = e.getKey();
//            Double need = RecommendedNutrientConstants.getRecommendedValue(nutrient, toStageKey(stage));
//            if (need == null || need <= 0) continue;
//            double have = totals.getOrDefault(nutrient, 0.0);
//            double ratio = have / need;
//            if (ratio < threshold) {
//                under.add(Map.entry(nutrient, ratio));
//            }
//        }
//        return under.stream()
//                .sorted(Comparator.comparingDouble(Map.Entry::getValue)) // 가장 부족한 순
//                .limit(topN)
//                .map(Map.Entry::getKey)
//                .toList();
//    }
//
//    private List<String> pickAchievedTags(Map<String, Double> totals, PregnancyStageUtil.Stage stage, List<String> keys) {
//        List<String> achieved = new ArrayList<>();
//        for (String k : keys) {
//            Double need = RecommendedNutrientConstants.getRecommendedValue(k, toStageKey(stage));
//            if (need == null || need <= 0) continue;
//            double have = totals.getOrDefault(k, 0.0);
//            if (have >= need) achieved.add(k);
//        }
//        return achieved;
//    }
//
//    private String makeDeficitBody(List<String> tags) {
//        // ex) "오늘 비타민 D와 오메가-3가 권장량보다 부족해요."
//        return "오늘 " + String.join("·", toKoreanTags(tags)) + "가 권장량보다 부족해요.";
//    }
//    private String makeGoalBody(List<String> tags) {
//        return "오늘 " + String.join("·", toKoreanTags(tags)) + " 목표를 달성했어요! 잘하셨어요 👏";
//    }
//    private List<String> toKoreanTags(List<String> tags) {
//        // TODO: 표시명 매핑 필요시 테이블/맵으로
//        return tags;
//    }
//    private String toStageKey(PregnancyStageUtil.Stage s) {
//        return switch (s) { case EARLY -> "EARLY"; case MID -> "MID"; case LATE -> "LATE"; };
//    }
//    private String buildMealDeepLink(MealType meal, LocalDate date) {
//        // 앱 딥링크가 확정 안됐으면 **웹 경로**로도 충분: FE에서 분기
//        return "app://food/add?meal=" + meal.name() + "&date=" + date;
//        // 또는 "/food/add?meal=...&date=..." (앱/웹 공용 라우팅이면)
//    }
//    private String mealKorean(MealType meal) {
//        return switch (meal) { case BREAKFAST -> "아침"; case LUNCH -> "점심"; case DINNER -> "저녁"; };
//    }
}
