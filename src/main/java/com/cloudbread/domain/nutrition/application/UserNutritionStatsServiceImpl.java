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
