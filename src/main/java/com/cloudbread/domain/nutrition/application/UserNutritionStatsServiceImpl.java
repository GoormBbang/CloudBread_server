package com.cloudbread.domain.nutrition.application;

import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
//import com.cloudbread.domain.nutrition.domain.repository.NutritionFoodNutrientRepository;
import com.cloudbread.domain.nutrition.constant.RecommendedNutrientConstants;
import com.cloudbread.domain.nutrition.dto.TodayNutrientsStatsDto;
import com.cloudbread.domain.nutrition.model.NutrientCalculationResult;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;

import com.cloudbread.domain.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserNutritionStatsServiceImpl implements UserNutritionStatsService {

    private final UserFoodHistoryRepository userFoodHistoryRepository;
    private final FoodNutrientRepository foodNutrientRepository;
    private final UserRepository userRepository;

    // ✅ 임산부 일일 권장 섭취량
    private static final double FOLIC_ACID_DRI = 600.0;  // μg
    private static final double CALCIUM_DRI = 930.0;    // mg
    private static final double IRON_DRI = 24.0;        // mg

    /**
     * 오늘의 영양 분석 수행
     */
    @Override
    public TodayNutrientsStatsDto calculateTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        log.info("===== [영양 분석 시작] userId={} / 기간: {} ~ {} =====", userId, startOfDay, endOfDay);

        // ✅ 1. 오늘 먹은 음식 기록 조회
        List<UserFoodHistory> todayFoodHistory = userFoodHistoryRepository
                .findByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);

        if (todayFoodHistory.isEmpty()) {
            log.info("오늘 섭취 기록이 없습니다. → 모든 영양소 0 반환");
            return TodayNutrientsStatsDto.createEmpty(userId, today);
        }

        log.info("섭취 기록 수: {}", todayFoodHistory.size());

        // ✅ 2. 영양소 계산
        NutrientCalculationResult result = calculateNutrients(todayFoodHistory);

        // ✅ 3. 결과 DTO 생성
        return buildResponseDto(userId, today, result);
    }

    /**
     * 실제 영양소 계산 로직
     */
    private NutrientCalculationResult calculateNutrients(List<UserFoodHistory> foodHistoryList) {
        log.info("=== [3단계] 영양소 정보 조회 및 합산 시작 ===");

        // 1️⃣ 오늘 먹은 음식 ID 추출
        List<Long> foodIds = foodHistoryList.stream()
                .map(history -> history.getFood().getId())
                .distinct()
                .toList();

        // 2️⃣ 음식별 영양소 데이터 조회 (엽산, 칼슘, 철분)
        List<FoodNutrient> allNutrients = foodNutrientRepository.findNutrientsByFoodIdsAndNames(
                foodIds,
                List.of("FOLIC_ACID", "엽산", "CALCIUM", "칼슘", "IRON", "철분")
        );

        // 3️⃣ foodId별 그룹화
        Map<Long, List<FoodNutrient>> nutrientsByFoodId = allNutrients.stream()
                .collect(Collectors.groupingBy(fn -> fn.getFood().getId()));

        // 4️⃣ 누적 변수
        double totalFolicAcid = 0.0;
        double totalCalcium = 0.0;
        double totalIron = 0.0;

        // 5️⃣ 각 음식별로 섭취 비율 * 영양값 계산
        for (UserFoodHistory history : foodHistoryList) {
            Long foodId = history.getFood().getId();
            double intakeRatio = history.getIntakePercent() / 100.0;

            List<FoodNutrient> nutrients = nutrientsByFoodId.get(foodId);
            if (nutrients == null || nutrients.isEmpty()) continue;

            for (FoodNutrient nutrient : nutrients) {
                String name = nutrient.getNutrient().getName().toUpperCase();
                double adjustedValue = nutrient.getValue().doubleValue() * intakeRatio;

                switch (name) {
                    case "FOLIC_ACID", "엽산" -> totalFolicAcid += adjustedValue;
                    case "CALCIUM", "칼슘" -> totalCalcium += adjustedValue;
                    case "IRON", "철분" -> totalIron += adjustedValue;
                }
            }
        }

        log.info("[계산 완료] 엽산={}μg, 칼슘={}mg, 철분={}mg",
                totalFolicAcid, totalCalcium, totalIron);

        return new NutrientCalculationResult(totalFolicAcid, totalCalcium, totalIron);
    }

    /**
     * DTO 빌드 (userId, 날짜, nutrient 리스트)
     */
    private TodayNutrientsStatsDto buildResponseDto(Long userId, LocalDate date, NutrientCalculationResult result) {
        // 섭취량 반올림(소수 1자리) – 필요하면 2로 바꾸세요
        BigDecimal folicAmt  = scale(result.getFolicAcid(), 1);
        BigDecimal calciumAmt= scale(result.getCalcium(), 1);
        BigDecimal ironAmt   = scale(result.getIron(), 1);

        // 퍼센트는 정수 반올림
        int folicPct  = pct(result.getFolicAcid(), FOLIC_ACID_DRI);
        int calciumPct= pct(result.getCalcium(),  CALCIUM_DRI);
        int ironPct   = pct(result.getIron(),     IRON_DRI);

        List<TodayNutrientsStatsDto.NutrientDetail> nutrients = List.of(
                TodayNutrientsStatsDto.NutrientDetail.builder()
                        .name("엽산").value(folicPct).unit(folicAmt).build(),
                TodayNutrientsStatsDto.NutrientDetail.builder()
                        .name("칼슘").value(calciumPct).unit(calciumAmt).build(),
                TodayNutrientsStatsDto.NutrientDetail.builder()
                        .name("철분").value(ironPct).unit(ironAmt).build()
        );

        return TodayNutrientsStatsDto.builder()
                .userId(userId)
                .createdAt(date)
                .nutrients(nutrients)
                .build();
    }

    private static BigDecimal scale(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
    }

    private static int pct(double actual, double dri) {
        if (dri <= 0) return 0;
        return (int)Math.round(actual / dri * 100.0);
    }

    @Override
    public List<TodayNutrientsStatsDto> getTodaySummary(Long userId, LocalDate date) {
        log.info("[Nutrition] 요약 조회 userId={}, date={}", userId, date);

        // 1️⃣ 오늘 먹은 식단 조회
        List<UserFoodHistory> histories =
                userFoodHistoryRepository.findByUserIdAndDateWithFood(userId, date);

        if (histories.isEmpty()) {
            log.warn("[Nutrition] 식단 기록 없음");
            return Collections.emptyList();
        }

        // 2️⃣ 섭취한 음식 ID 추출
        List<Long> foodIds = histories.stream()
                .map(h -> h.getFood().getId())
                .distinct()
                .toList();

        // 3️⃣ 음식별 영양소 + intake_percent 반영하여 총합 계산
        Map<String, Double> intakeMap = calculateIntakeWithPercent(histories, foodIds);

        // 4️⃣ 임신 주차 / 단계 계산
        LocalDate dueDate = userRepository.findDueDateByUserId(userId);
        if (dueDate == null) {
            log.warn("[Nutrition] 출산 예정일 정보 없음 - userId={}", userId);
            return Collections.emptyList();
        }

        int pregnancyWeek = calculatePregnancyWeek(dueDate);
        String stage = determinePregnancyStage(pregnancyWeek);
        log.info("[Nutrition] 현재 임신 {}주차 - 단계: {}", pregnancyWeek, stage);

        // 5️⃣ 부족 영양소 계산
        NutrientCalculationResult result = calculateDeficiency(intakeMap, stage);
        String comment = (result.getDeficientNutrient() == null)
                ? "목표 달성"
                : "목표 미달";

        return List.of(
                TodayNutrientsStatsDto.builder()
                        .totalCalories((int) Math.round(intakeMap.getOrDefault("CALORIES", 0.0)))
                        .comment(comment)
                        .lackedValue((double) Math.round(result.getDeficientValue())) // ✅ 소수점 첫째 자리에서 반올림
                        .lackedNutrient(result.getDeficientNutrient())
                        .build()
        );


    }

    // 🔹 (A) 음식별 섭취량 계산
    private Map<String, Double> calculateIntakeWithPercent(List<UserFoodHistory> histories, List<Long> foodIds) {
        List<FoodNutrient> nutrients = foodNutrientRepository.findByFoodIdsWithNutrient(foodIds);
        Map<String, Double> total = new HashMap<>();

        for (UserFoodHistory history : histories) {
            double factor = history.getIntakePercent() / 100.0;
            Long foodId = history.getFood().getId();

            nutrients.stream()
                    .filter(fn -> fn.getFood().getId().equals(foodId))
                    .forEach(fn -> {
                        String name = normalizeNutrientName(fn.getNutrient().getName());
                        total.merge(name, fn.getValue().doubleValue() * factor, Double::sum);
                    });
        }

        // 칼로리도 포함
        double totalCalories = histories.stream()
                .mapToDouble(h -> Optional.ofNullable(h.getFood().getCalories())
                        .map(Number::doubleValue)
                        .orElse(0.0) * (h.getIntakePercent() / 100.0))
                .sum();
        total.put("CALORIES", totalCalories);

        log.info("[Nutrition] 총 섭취량 계산 결과: {}", total);
        return total;
    }

    // 🔹 (B) 영양소 이름 통일
    private String normalizeNutrientName(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^가-힣A-Za-z]", "").toUpperCase(); // 단백질(g) → 단백질 → DANBAEGIL → UPPER
    }

    // 🔹 (C) 임신 단계별 권장 섭취량
    private Map<String, Double> getRecommendedIntake(String stage) {
        Map<String, Double> rec = new HashMap<>();

        switch (stage) {
            case "EARLY" -> {
                rec.put("PROTEINS", 60.0);
                rec.put("CARBS", 300.0);
                rec.put("FATS", 60.0);
                rec.put("CALCIUM", 900.0);
                rec.put("IRON", 20.0);
                rec.put("FOLIC_ACID", 480.0);
                rec.put("SODIUM", 1500.0);
                rec.put("CHOLESTEROL", 300.0);
                rec.put("TRANS_FAT", 2.0);
                rec.put("SATURATED_FAT", 15.0);
                rec.put("SUGARS", 50.0);
                rec.put("MOISTURE", 2000.0);
            }
            case "MIDDLE" -> {
                rec.put("PROTEINS", 70.0);
                rec.put("CARBS", 320.0);
                rec.put("FATS", 70.0);
                rec.put("CALCIUM", 950.0);
                rec.put("IRON", 24.0);
                rec.put("FOLIC_ACID", 480.0);
                rec.put("SODIUM", 1500.0);
                rec.put("CHOLESTEROL", 300.0);
                rec.put("TRANS_FAT", 2.0);
                rec.put("SATURATED_FAT", 15.0);
                rec.put("SUGARS", 50.0);
                rec.put("MOISTURE", 2000.0);
            }
            case "LATE" -> {
                rec.put("PROTEINS", 80.0);
                rec.put("CARBS", 340.0);
                rec.put("FATS", 70.0);
                rec.put("CALCIUM", 1000.0);
                rec.put("IRON", 27.0);
                rec.put("FOLIC_ACID", 500.0);
                rec.put("SODIUM", 1500.0);
                rec.put("CHOLESTEROL", 300.0);
                rec.put("TRANS_FAT", 2.0);
                rec.put("SATURATED_FAT", 15.0);
                rec.put("SUGARS", 50.0);
                rec.put("MOISTURE", 2000.0);
            }
            default -> {
                rec.put("PROTEINS", 65.0);
                rec.put("CARBS", 310.0);
                rec.put("FATS", 65.0);
                rec.put("CALCIUM", 900.0);
                rec.put("IRON", 18.0);
                rec.put("FOLIC_ACID", 400.0);
                rec.put("SODIUM", 1500.0);
                rec.put("CHOLESTEROL", 300.0);
                rec.put("TRANS_FAT", 2.0);
                rec.put("SATURATED_FAT", 15.0);
                rec.put("SUGARS", 50.0);
                rec.put("MOISTURE", 2000.0);
            }
        }
        return rec;
    }

    private NutrientCalculationResult calculateDeficiency(Map<String, Double> intakeMap, String stage) {
        Map<String, Double> recommendedIntake = getRecommendedIntake(stage);

        String lackingNutrient = null;
        double lackingValue = 0.0;

        log.info("[Nutrition] 영양소 섭취량 비교 (단위: g)");
        log.info("임신 단계: {}", stage);
        log.info("--------------------------------------------");
        log.info("영양소 | 섭취량 | 권장량 | 부족량");

        for (Map.Entry<String, Double> entry : recommendedIntake.entrySet()) {
            String nutrient = entry.getKey();
            double recommended = entry.getValue();
            double intake = intakeMap.getOrDefault(nutrient, 0.0);
            double lack = Math.max(0, recommended - intake);

            log.info("{} | {} | {} | {}",
                    nutrient,
                    String.format("%.2f", intake),
                    String.format("%.2f", recommended),
                    String.format("%.2f", lack)
            );

            if (lack > lackingValue) {
                lackingValue = lack;
                lackingNutrient = nutrient;
            }
        }

        log.info("--------------------------------------------");
        log.info("[Nutrition] 부족 영양소 계산 결과 => {}: {}g 부족",
                lackingNutrient, String.format("%.2f", lackingValue)
        );


        log.info("--------------------------------------------");

        return new NutrientCalculationResult(lackingNutrient, lackingValue);
    }

    // 🔹 (E) 임신 주차 계산
    private int calculatePregnancyWeek(LocalDate dueDate) {
        LocalDate today = LocalDate.now();
        long weeks = ChronoUnit.WEEKS.between(dueDate.minusWeeks(40), today);
        return (int) Math.max(weeks, 0);
    }

    // 🔹 (F) 임신 단계 구분
    private String determinePregnancyStage(int week) {
        if (week <= 12) return "EARLY";
        else if (week <= 27) return "MIDDLE";
        else return "LATE";
    }

}
