package com.cloudbread.domain.notifiaction.application;

import com.cloudbread.domain.notifiaction.domain.Notification;
import com.cloudbread.domain.notifiaction.domain.NotificationType;
import com.cloudbread.domain.notifiaction.repository.NotificationRepository;

import com.cloudbread.domain.user.domain.entity.User;

import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.cloudbread.domain.nutrition.constant.RecommendedNutrientConstants.getRecommendedValue;
/**
 * - 모든 활성유저(또는 대상유저)를 훑는다
 * - 당일 섭취 합계 구해서 부족 top2~3, 엽산/칼슘/철분 달성 판단 -> Notification 생성 & 저장
 * - sendNow=true면 pushIfConnected 호출
 *
 * - NUTRIENT_DEFICIT: 당일 섭취량 / 임신단계별 권장량 < 0.9 인 영양소 Top2~3 묶어 1건 -> 90% 미만
 * - NUTRIENT_GOAL_ACHIEVED: 엽산(FOLIC_ACID)·칼슘(CALCIUM)·철분(IRON) 중 달성(≥100%) 항목들을 묶어 1건
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerService {

    private static final double DEFICIT_THRESHOLD = 0.90; // 90%
    private static final int DEFICIT_TOP_N = 3;
    private static final List<String> GOAL_KEYS = List.of("FOLIC_ACID", "CALCIUM", "IRON");

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPushService notificationPushService;
    private final UserFoodHistoryRepository userFoodHistoryRepository;

    private User getTargetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    }

    /** [REAL] 21:00 일일 요약: 부족/목표 달성 각각 조건 만족 시에만 생성 */
    @Transactional
    public void generateDaily(Long userId, LocalDate dateOrNull, boolean sendNow) {
        User user = getTargetUser(userId);
        LocalDate date = (dateOrNull != null) ? dateOrNull : LocalDate.now();

        // (선택) 중복 방지: 같은 날 같은 타입 이미 보냈으면 Skip
//        if (notificationRepository.existsByUserIdAndTypeOnDate(user.getId(), NotificationType.NUTRIENT_DEFICIT, date)) {
//            log.info("[DAILY] deficit already sent. userId={}, date={}", userId, date);
//        } else {
            handleDeficit(user, date, sendNow);
//        }

//        if (notificationRepository.existsByUserIdAndTypeOnDate(user.getId(), NotificationType.NUTRIENT_GOAL_ACHIEVED, date)) {
//            log.info("[DAILY] goal already sent. userId={}, date={}", userId, date);
//        } else {
            handleGoal(user, date, sendNow);
//        }
    }

    private void handleDeficit(User user, LocalDate date, boolean sendNow) {
        String stage = determineStage(user.getDueDate(), date); // "EARLY" | "MID" | "LATE"
        Map<String, Double> totals = loadTotalsAsMap(user.getId(), date);

        log.info("[DAILY/DEFICIT] userId={}, date={}, stage={}, totals={}",
                user.getId(), date, stage, totals);

        // 부족한 영양소 ratio(=have/need) 기준으로 오름차순 → 상위 2~3개
        List<Map.Entry<String, Double>> underList = new ArrayList<>();
        for (var e : totals.entrySet()) {
            String key = e.getKey().toUpperCase(Locale.ROOT);
            Double need = getRecommendedValue(key, stage);
            if (need == null || need <= 0) continue;
            double have = Optional.ofNullable(e.getValue()).orElse(0.0);
            double ratio = have / need;
            if (ratio < DEFICIT_THRESHOLD) {
                underList.add(Map.entry(key, ratio));
            }
        }

        if (underList.isEmpty()) {
            log.info("[DAILY/DEFICIT] no deficit. userId={}, date={}", user.getId(), date);
            return; // 아무 것도 안보냄
        }

        var topEn = underList.stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue)) // 가장 부족한 순
                .limit(DEFICIT_TOP_N)
                .map(Map.Entry::getKey)
                .toList();

        var topKo = toKoreanTags(topEn); // 한글로 변환
        String body = "오늘 " + String.join("·", topKo) + " 영양소가 권장량보다 부족해요.";
        Notification n = Notification.create(
                user,
                NotificationType.NUTRIENT_DEFICIT,
                "영양소 부족 알림",
                body,
                topKo,   // 한글 태그로 저장
                null
        );
//        String body = "오늘 " + String.join("·", toKoreanTags(top)) + " 영양소가 권장량보다 부족해요.";
//        Notification n = Notification.create(
//                user,
//                NotificationType.NUTRIENT_DEFICIT,
//                "영양소 부족 알림",
//                body,
//                top,   // tags
//                null   // 딥링크 없음(정보성)
//        );
        notificationRepository.save(n);
        if (sendNow) notificationPushService.pushIfConnected(n);

        log.info("[DAILY/DEFICIT] saved id={} userId={} date={} tags={}", n.getId(), user.getId(), date, topKo);
    }

    private void handleGoal(User user, LocalDate date, boolean sendNow) {
        String stage = determineStage(user.getDueDate(), date);
        Map<String, Double> totals = loadTotalsAsMap(user.getId(), date);

        log.info("[DAILY/GOAL] userId={}, date={}, stage={}, totals(keys)={}",
                user.getId(), date, stage, totals.keySet());

        List<String> achievedEn = new ArrayList<>();
        for (String key : GOAL_KEYS) {
            Double need = getRecommendedValue(key, stage);
            if (need == null || need <= 0) continue;
            double have = totals.getOrDefault(key, 0.0);
            if (have >= need) achievedEn.add(key);
        }
        if (achievedEn.isEmpty()) return;

        var achievedKo = toKoreanTags(achievedEn); // ✅ 한글로 변환
        String body = "오늘 " + String.join("·", achievedKo) + " 목표를 달성했어요! 잘하셨어요 👏";
        Notification n = Notification.create(
                user,
                NotificationType.NUTRIENT_GOAL_ACHIEVED,
                "목표 달성",
                body,
                achievedKo, // ✅ 한글 태그로 저장
                null
        );

        notificationRepository.save(n);
        if (sendNow) notificationPushService.pushIfConnected(n);

        log.info("[DAILY/GOAL] saved id={} userId={} date={} tags={}", n.getId(), user.getId(), date, achievedEn);
    }

    /** 당일 합계를 Map으로
     * - 당일 섭취량 계산
     * */
    private Map<String, Double> loadTotalsAsMap(Long userId, LocalDate date) {
        var start = date.atStartOfDay();
        var end = start.plusDays(1);
        var rows = userFoodHistoryRepository.sumDailyNutrientsRaw(userId, start, end);
        Map<String, Double> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((String) r[0]).toUpperCase(), ((Number) r[1]).doubleValue());
        }
        return map;
    }

    /** 임신 단계 계산: dueDate - 40주 = 임신 시작(주차 0). Early ≤12, Mid ≤27, 나머지 Late */
    private String determineStage(LocalDate dueDate, LocalDate onDate) {
        if (dueDate == null) return "EARLY"; // 안전 기본값
        LocalDate start = dueDate.minusWeeks(40);
        long weeks = Math.max(0, ChronoUnit.WEEKS.between(start, onDate));
        if (weeks <= 12) return "EARLY";
        if (weeks <= 27) return "MID";
        return "LATE";
    }

    /** 표시명 매핑 필요하면 여기서 변환 (지금은 키 그대로 사용) */
    private List<String> toKoreanTags(List<String> keys) {
        // 예시 매핑: Map.of("FOLIC_ACID","엽산","CALCIUM","칼슘","IRON","철분")
        return keys.stream().map(k -> switch (k) {
            case "FOLIC_ACID" -> "엽산";
            case "CALCIUM" -> "칼슘";
            case "IRON" -> "철분";
            case "PROTEINS" -> "단백질";
            case "CARBS" -> "탄수화물";
            case "FATS" -> "지방";
            case "SODIUM" -> "나트륨";
            case "SUGARS" -> "당류";
            case "CHOLESTEROL" -> "콜레스테롤";
            case "SATURATED_FAT" -> "포화지방";
            case "MOISTURE" -> "수분";
            default -> k;
        }).toList();
    }
}


//    @Transactional
//    public void generateMealMissedFake(Long userId, LocalDate date, MealType meal, boolean sendNow) {
//
//    }

//   더미 save
//    /** [FAKE] 21:00 일일요약: 부족 1건 + 목표 1건을 하드코딩 생성 */
//    @Transactional
//    public void generateDailyFake(Long userId, LocalDate dateOrNull, boolean sendNow) {
//        User user = getTargetUser(userId);
//
//        // 1) 영양소 부족(그럴듯한 태그 2개)
//        Notification deficit = Notification.create(
//                user,
//                NotificationType.NUTRIENT_DEFICIT,
//                "영양소 부족 알림",
//                "오늘 엽산과 칼슘이 권장량보다 부족해요.",
//                List.of("FOLIC_ACID", "CALCIUM"),
//                null // 정보성이라 딥링크 없음
//        );
//        notificationRepository.save(deficit);
//        if (sendNow) notificationPushService.pushIfConnected(deficit);
//
//        // 2) 목표 달성(중요 3종 중 2개 달성했다고 가정)
//        Notification goal = Notification.create(
//                user,
//                NotificationType.NUTRIENT_GOAL_ACHIEVED,
//                "목표 달성",
//                "오늘 엽산·철분 목표를 달성했어요! 잘하셨어요 👏",
//                List.of("FOLIC_ACID", "IRON"),
//                null
//        );
//        notificationRepository.save(goal);
//        if (sendNow) notificationPushService.pushIfConnected(goal);
//
//        log.info("[FAKE-DAILY] userId={}, created deficit(id={}), goal(id={})",
//                userId, deficit.getId(), goal.getId());
//    }
//
//    /** [FAKE] 끼니 누락 1건을 하드코딩 생성 */
//    @Transactional
//    public void generateMealMissedFake(Long userId, LocalDate date, MealType meal, boolean sendNow) {
//        User user = getTargetUser(userId);
//
//        String body = switch (meal) {
//            case BREAKFAST -> "아침 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
//            case LUNCH     -> "점심 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
//            case DINNER    -> "저녁 식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
//            default        -> "식사를 기록하지 않았어요. 정확한 영양 관리를 위해 기록해주세요.";
//        };
//
//      //  String deepLink = "app://food/add?meal=" + meal.name() + "&date=" + date;
//
//        Notification missed = Notification.create(
//                user,
//                NotificationType.MEAL_LOG_MISSED,
//                "식단 기록 누락",
//                body,
//                List.of(meal.name()),     // ["LUNCH"] 같은 태그
//                null                  // 눌렀을 때 식단 추가로 이동
//        );
//        notificationRepository.save(missed);
//        if (sendNow) notificationPushService.pushIfConnected(missed);
//
//        log.info("[FAKE-MISSED] userId={}, meal={}, id={}", userId, meal, missed.getId());
//    }


