package com.cloudbread.domain.nutrition.application;

import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
//import com.cloudbread.domain.nutrition.domain.repository.NutritionFoodNutrientRepository;
import com.cloudbread.domain.nutrition.dto.TodayNutrientsStatsDto;
import com.cloudbread.domain.nutrition.model.NutrientCalculationResult;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;

///**
// * 📊 사용자의 ‘오늘 섭취 영양 통계’를 계산하는 서비스 구현체
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional(readOnly = true)
//public class UserNutritionStatsServiceImpl implements UserNutritionStatsService {
//
//    private final UserFoodHistoryRepository userFoodHistoryRepository;
//    private final NutritionFoodNutrientRepository foodNutrientRepository;
//
//    // 임산부 일일 권장 섭취량 (DRI)
//    private static final double FOLIC_ACID_DRI = 600.0; // μg (엽산)
//    private static final double CALCIUM_DRI = 930.0;    // mg (칼슘)
//    private static final double IRON_DRI = 24.0;        // mg (철분)
//
//    // ====================================================================
//    // 1️⃣ 전체 로직의 진입점
//    // ====================================================================
//    @Override
//    public TodayNutrientsStatsDto calculateTodayStats(Long userId) {
//        log.info("===== 영양 분석 시작 - userId: {} =====", userId);
//
//        // 2단계: 오늘의 섭취 기록 조회
//        List<UserFoodHistory> todayFoodHistory = getTodayFoodHistory(userId);
//
//        if (todayFoodHistory.isEmpty()) {
//            log.info("오늘 섭취 기록 없음 - 모든 영양소 0% 반환");
//            return TodayNutrientsStatsDto.createEmpty(LocalDate.now());
//        }
//
//        // 3단계: 영양소 정보 조회
//        Map<Long, List<FoodNutrient>> nutrientsMap = fetchNutrientsForFoods(todayFoodHistory);
//
//        // 4단계: 영양소별 합산
//        NutrientTotals totals = calculateNutrientTotals(todayFoodHistory, nutrientsMap);
//
//        // 5단계: 비율 계산 및 DTO 변환
//        return createResponseDto(totals);
//    }
//
//    // ====================================================================
//    // 2️⃣ 오늘의 섭취 기록 조회
//    // ====================================================================
//    private List<UserFoodHistory> getTodayFoodHistory(Long userId) {
//        LocalDate today = LocalDate.now();
//        LocalDateTime startOfDay = today.atStartOfDay();
//        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
//
//        log.info("[2단계] 섭취 기록 조회 - 날짜: {}", today);
//
//        List<UserFoodHistory> histories = userFoodHistoryRepository
//                .findByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
//
//        log.info("[2단계] 조회 결과: {}개 기록", histories.size());
//        histories.forEach(h -> log.debug("  - {} [{}]: {}%", h.getFood().getName(), h.getMealType(), h.getIntakePercent()));
//
//        return histories;
//    }
//
//    // ====================================================================
//    // 3️⃣ 영양소 정보 조회
//    // ====================================================================
//    private Map<Long, List<FoodNutrient>> fetchNutrientsForFoods(List<UserFoodHistory> foodHistories) {
//        log.info("[3단계] 영양소 정보 조회 시작");
//
//        List<Long> foodIds = foodHistories.stream()
//                .map(h -> h.getFood().getId())
//                .distinct()
//                .toList();
//
//        List<FoodNutrient> nutrients = foodNutrientRepository
//                .findNutrientsByFoodIdsAndNames(
//                        foodIds,
//                        List.of("FOLIC_ACID", "엽산", "CALCIUM", "칼슘", "IRON", "철분")
//                );
//
//        log.info("[3단계] 조회된 영양소 정보: {}개", nutrients.size());
//        return nutrients.stream().collect(Collectors.groupingBy(fn -> fn.getFood().getId()));
//    }
//
//    // ====================================================================
//    // 4️⃣ 영양소별 합산 계산
//    // ====================================================================
//    private NutrientTotals calculateNutrientTotals(
//            List<UserFoodHistory> foodHistories,
//            Map<Long, List<FoodNutrient>> nutrientsMap) {
//
//        log.info("[4단계] 영양소 합산 계산 시작");
//
//        double totalFolicAcid = 0.0;
//        double totalCalcium = 0.0;
//        double totalIron = 0.0;
//
//        for (UserFoodHistory history : foodHistories) {
//            Long foodId = history.getFood().getId();
//            double intakeRatio = history.getIntakePercent() / 100.0;
//
//            List<FoodNutrient> nutrients = nutrientsMap.get(foodId);
//            if (nutrients == null || nutrients.isEmpty()) {
//                log.warn("[4단계] 음식 ID {} 의 영양소 정보 없음", foodId);
//                continue;
//            }
//
//            for (FoodNutrient nutrient : nutrients) {
//                String nutrientName = nutrient.getNutrient().getName().toUpperCase();
//                double adjustedValue = nutrient.getValue().doubleValue() * intakeRatio;
//
//                switch (nutrientName) {
//                    case "FOLIC_ACID", "엽산" -> totalFolicAcid += adjustedValue;
//                    case "CALCIUM", "칼슘" -> totalCalcium += adjustedValue;
//                    case "IRON", "철분" -> totalIron += adjustedValue;
//                }
//            }
//        }
//
//        log.info("[4단계] 합산 완료 - 엽산: {}μg, 칼슘: {}mg, 철분: {}mg", totalFolicAcid, totalCalcium, totalIron);
//        return new NutrientTotals(totalFolicAcid, totalCalcium, totalIron);
//    }
//
//    // ====================================================================
//    // 5️⃣ Response DTO 생성
//    // ====================================================================
//    private TodayNutrientsStatsDto createResponseDto(NutrientTotals totals) {
//        log.info("[5단계] 권장량 대비 퍼센트 계산 시작");
//
//        int folicAcidPercent = calculatePercentage(totals.getFolicAcid(), FOLIC_ACID_DRI);
//        int calciumPercent = calculatePercentage(totals.getCalcium(), CALCIUM_DRI);
//        int ironPercent = calculatePercentage(totals.getIron(), IRON_DRI);
//
//        log.info("[5단계] 최종 달성률 - 엽산: {}%, 칼슘: {}%, 철분: {}%",
//                folicAcidPercent, calciumPercent, ironPercent);
//
//        return TodayNutrientsStatsDto.builder()
//                .analysisDate(LocalDate.now())
//                .folicAcidPercent(folicAcidPercent)
//                .calciumPercent(calciumPercent)
//                .ironPercent(ironPercent)
//                .build();
//    }
//
//    private int calculatePercentage(double actual, double recommended) {
//        if (recommended <= 0) return 0;
//        int percentage = (int) Math.round((actual / recommended) * 100);
//        return Math.min(percentage, 100);
//    }
//
//    // 내부 클래스 - 영양소 합계
//    @Getter
//    @AllArgsConstructor
//    private static class NutrientTotals {
//        private final double folicAcid;
//        private final double calcium;
//        private final double iron;
//    }
//}

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserNutritionStatsServiceImpl implements UserNutritionStatsService {

    private final UserFoodHistoryRepository userFoodHistoryRepository;
    private final FoodNutrientRepository foodNutrientRepository;

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
}
