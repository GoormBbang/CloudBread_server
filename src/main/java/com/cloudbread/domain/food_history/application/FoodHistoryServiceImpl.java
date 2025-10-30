package com.cloudbread.domain.food_history.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.food_history.dto.*;
import com.cloudbread.domain.nutrition.constant.RecommendedNutrientConstants;
import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import com.cloudbread.domain.photo_analyses.domain.repository.PhotoAnalysisRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.cloudbread.domain.user.domain.enums.MealType;
import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FoodHistoryServiceImpl implements FoodHistoryService {
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final UserFoodHistoryRepository foodHistoryRepository;
    private final PhotoAnalysisRepository photoAnalysisRepository;

    private final UserFoodHistoryRepository userFoodHistoryRepository;
    private final FoodNutrientRepository foodNutrientRepository;
    private final UserFoodHistoryRepository historyRepository;

    @Override
    public FoodHistoryResponse.Created create(Long userId, FoodHistoryRequest.Create req) {
        // ── 기본 검증
        if (req.getFoodId() == null) throw new ValidationException("foodId is required");
        if (req.getMealType() == null) throw new ValidationException("mealType is required");
        if (req.getIntakePercent() < 0 || req.getIntakePercent() > 100)
            throw new ValidationException("intakePercent must be 0~100");

        // ── 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        Food food = foodRepository.findById(req.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("food 데이터가 존재하지 않습니다"));

        // ── photoAnalysisId는 '선택'이므로 검증 없이, 있으면 연관만 걸고 없거나 못 찾으면 null
        PhotoAnalysis pa = null;
        if (req.getPhotoAnalysisId() != null) {
            pa = photoAnalysisRepository.findById(req.getPhotoAnalysisId()).orElse(null);
        }

        // ── 저장

        var eatenAt = (req.getEatenAt() != null) ? req.getEatenAt() : LocalDateTime.now();
        UserFoodHistory saved = foodHistoryRepository.save(
                UserFoodHistory.of(user, food, pa, req.getMealType(), req.getIntakePercent(), eatenAt)
        );

        var selected = FoodHistoryResponse.SelectedFood.builder()
                .foodId(food.getId())
                .name(food.getName())
                .build();

        return FoodHistoryResponse.Created.builder()
                .historyId(saved.getId())
                .mealType(saved.getMealType().name())
                .intakePercent(saved.getIntakePercent())
                .eatenAt(saved.getCreatedAt())
                .selectedFood(selected)
                .build();
    }

    // ─────────────────────────────────────────────
    // 월별 식단 기록 조회
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    @Override
    public FoodHistoryCalendarDto getMonthlyCalendar(Long userId, Integer year, Integer month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = lastDay.plusDays(1).atStartOfDay();

        //log.info("📆 [식단 캘린더 조회] userId={}, 기간: {} ~ {}", userId, start, end);

        List<UserFoodHistory> histories = foodHistoryRepository.findByUserIdAndCreatedAtBetween(userId, start, end);

        if (histories.isEmpty()) {
            return FoodHistoryCalendarDto.createEmpty(year, month);
        }

        // 날짜별 끼니 수 계산
        Map<Integer, Set<MealType>> mealsByDay = new HashMap<>();
        for (UserFoodHistory h : histories) {
            int day = h.getCreatedAt().getDayOfMonth();
            mealsByDay.computeIfAbsent(day, k -> new HashSet<>()).add(h.getMealType());
        }

        List<DayMealCountDto> days = mealsByDay.entrySet().stream()
                .map(entry -> DayMealCountDto.of(entry.getKey(), entry.getValue().size()))
                .sorted(Comparator.comparing(DayMealCountDto::getDay))
                .toList();

        return FoodHistoryCalendarDto.builder()
                .year(year)
                .month(month)
                .days(days)
                .build();
    }

    // ────────────────────────────────────────────────
    // 특정 날짜 상세 조회 (캘린더 일별 상세)
    // ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    @Override
    public FoodHistoryResponse.CalendarDailySummaryDto getDailySummary(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String pregnancyStage = getPregnancyStage(user);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Join Fetch로 Food 정보 함께 조회
        List<UserFoodHistory> dailyHistory = foodHistoryRepository
                .findByUserIdAndCreatedAtBetweenWithFood(userId, startOfDay, endOfDay);

        // 식단이 없을 경우 → 예외 발생 (false 응답 유도)
        if (dailyHistory.isEmpty()) {
            log.warn("[식단 상세 조회 실패] userId={}, date={}", userId, date);
            throw new IllegalArgumentException("식단이 없습니다.");
        }

        Map<MealType, List<UserFoodHistory>> historyByMeal =
                dailyHistory.stream().collect(Collectors.groupingBy(UserFoodHistory::getMealType));

        List<FoodHistoryResponse.MealSummaryDto> mealSummaries = new ArrayList<>();
        int totalDayCalories = 0;

        for (var entry : historyByMeal.entrySet()) {
            FoodHistoryResponse.MealSummaryDto mealSummary = calculateMealSummary(entry.getKey(), entry.getValue());
            mealSummaries.add(mealSummary);
            totalDayCalories += mealSummary.getTotalCalories();
        }

        FoodHistoryResponse.NutritionTotalsDto dayNutrition = calculateDayNutrition(dailyHistory);
        List<FoodHistoryResponse.CalendarDailySummaryDto.MealIntakeLevelDto> intakeMessages =
                generateIntakeMessageWithRecommended(historyByMeal, dayNutrition, pregnancyStage);

        return FoodHistoryResponse.CalendarDailySummaryDto.builder()
                .date(date)
                .totalCalories(totalDayCalories)
                .nutritionTotals(dayNutrition)
                .meals(mealSummaries)
                .intakeMessages(intakeMessages)
                .build();
    }

    // ────────────────────────────────────────────────
    // 임신 주차 계산
    // ────────────────────────────────────────────────
    private String getPregnancyStage(User user) {
        if (user.getDueDate() == null) return "MID";

        long weeksUntilDue = ChronoUnit.WEEKS.between(LocalDate.now(), user.getDueDate());
        long currentWeek = 40 - weeksUntilDue;

        if (currentWeek <= 12) return "EARLY";
        else if (currentWeek <= 28) return "MID";
        else return "LATE";
    }

    // ────────────────────────────────────────────────
    // 끼니별 요약 계산
    // ────────────────────────────────────────────────
    private FoodHistoryResponse.MealSummaryDto calculateMealSummary(MealType mealType, List<UserFoodHistory> histories) {
        List<FoodHistoryResponse.FoodItemDto> foods = histories.stream()
                .map(h -> {
                    Food food = h.getFood();

                    // ✅ BigDecimal -> double 변환
                    double calories = (food.getCalories() instanceof java.math.BigDecimal)
                            ? ((java.math.BigDecimal) food.getCalories()).doubleValue()
                            : ((Number) food.getCalories()).doubleValue();

                    int actualCalories = (int) (calories * h.getIntakePercent() / 100.0);

                    return FoodHistoryResponse.FoodItemDto.builder()
                            .foodName(food.getName())
                            .category(food.getCategory()) // imageUrl → category
                            .calories(actualCalories)
                            .build();
                })
                .toList();

        int totalCalories = foods.stream().mapToInt(FoodHistoryResponse.FoodItemDto::getCalories).sum();

        return FoodHistoryResponse.MealSummaryDto.builder()
                .mealType(mealType.name())
                .totalCalories(totalCalories)
                .foods(foods)
                .build();
    }


    // ────────────────────────────────────────────────
    // 하루 전체 영양소 계산
    // ────────────────────────────────────────────────
    private FoodHistoryResponse.NutritionTotalsDto calculateDayNutrition(List<UserFoodHistory> dailyHistory) {
        List<Long> foodIds = dailyHistory.stream()
                .map(h -> h.getFood().getId())
                .distinct()
                .toList();

        List<FoodNutrient> nutrients = foodNutrientRepository.findByFoodIdsWithNutrient(foodIds);
        Map<Long, List<FoodNutrient>> nutrientsByFood = nutrients.stream()
                .collect(Collectors.groupingBy(fn -> fn.getFood().getId()));

        double carbs = 0, protein = 0, fat = 0, sugar = 0;

        for (UserFoodHistory history : dailyHistory) {
            double ratio = history.getIntakePercent() / 100.0; // ✅ ratio는 여기서 선언
            List<FoodNutrient> fnList = nutrientsByFood.get(history.getFood().getId());
            if (fnList == null) continue;

            for (FoodNutrient fn : fnList) {
                String name = fn.getNutrient().getName().toUpperCase();

                // ✅ BigDecimal -> double 변환
                double val = (fn.getValue() instanceof java.math.BigDecimal)
                        ? ((java.math.BigDecimal) fn.getValue()).doubleValue() * ratio
                        : ((Number) fn.getValue()).doubleValue() * ratio;

                switch (name) {
                    case "CARBS", "탄수화물" -> carbs += val;
                    case "PROTEINS", "단백질" -> protein += val;
                    case "FATS", "지방" -> fat += val;
                    case "SUGARS", "당류" -> sugar += val;
                    default -> {}
                }
            }
        }

        return FoodHistoryResponse.NutritionTotalsDto.builder()
                .carbs((int) Math.round(carbs))
                .protein((int) Math.round(protein))
                .fat((int) Math.round(fat))
                .sugar((int) Math.round(sugar))
                .build();
    }

    // ────────────────────────────────────────────────
    // 권장량 대비 메시지 생성
    // ────────────────────────────────────────────────
    private List<FoodHistoryResponse.CalendarDailySummaryDto.MealIntakeLevelDto> generateIntakeMessageWithRecommended(
            Map<MealType, List<UserFoodHistory>> historyByMeal,
            FoodHistoryResponse.NutritionTotalsDto dayNutrition,
            String pregnancyStage) {

        Map<MealType, Integer> caloriesByMeal = new HashMap<>();
        for (var entry : historyByMeal.entrySet()) {
            int mealCalories = entry.getValue().stream()
                    .mapToInt(h -> {
                        double calories = (h.getFood().getCalories() instanceof java.math.BigDecimal)
                                ? ((java.math.BigDecimal) h.getFood().getCalories()).doubleValue()
                                : ((Number) h.getFood().getCalories()).doubleValue();
                        return (int) (calories * h.getIntakePercent() / 100.0);
                    })
                    .sum();
            caloriesByMeal.put(entry.getKey(), mealCalories);
        }

        String carbsLevel = getIntakeLevel("CARBS", dayNutrition.getCarbs(), pregnancyStage);
        String proteinLevel = getIntakeLevel("PROTEINS", dayNutrition.getProtein(), pregnancyStage);
        String fatLevel = getIntakeLevel("FATS", dayNutrition.getFat(), pregnancyStage);

        String overallLevel = determineOverallLevel(carbsLevel, proteinLevel, fatLevel);

        // JSON 형태로 반환할 리스트 생성
        List<FoodHistoryResponse.CalendarDailySummaryDto.MealIntakeLevelDto> intakeList = new ArrayList<>();

        for (MealType mealType : MealType.values()) {
            if (historyByMeal.containsKey(mealType)) {
                String mealName = getMealName(mealType);
                Integer mealCalories = caloriesByMeal.get(mealType);
                String mealLevel = adjustLevelByMealProportion(overallLevel, mealType, mealCalories);

                intakeList.add(
                        FoodHistoryResponse.CalendarDailySummaryDto.MealIntakeLevelDto.builder()
                                .mealType(mealName)
                                .level(mealLevel)
                                .build()
                );
            }
        }

        return intakeList;
    }


    // ────────────────────────────────────────────────
    // 영양소 섭취 수준 비교
    // ────────────────────────────────────────────────
    private String getIntakeLevel(String nutrientName, Integer actualAmount, String pregnancyStage) {
        Double recommended = RecommendedNutrientConstants.getRecommendedValue(nutrientName, pregnancyStage);
        if (recommended == null || recommended == 0) return "적당히";

        double ratio = actualAmount / recommended;
        if (ratio < 0.7) return "적게";
        else if (ratio <= 1.3) return "적당히";
        else return "많이";
    }

    private String determineOverallLevel(String carbs, String protein, String fat) {
        double score = (getLevelScore(carbs) + getLevelScore(protein) + getLevelScore(fat)) / 3.0;
        if (score < 1.5) return "적게";
        else if (score < 2.5) return "적당히";
        else return "많이";
    }

    private int getLevelScore(String level) {
        return switch (level) {
            case "적게" -> 1;
            case "적당히" -> 2;
            case "많이" -> 3;
            default -> 2;
        };
    }

    // ────────────────────────────────────────────────
    // 끼니별 수준 보정
    // ────────────────────────────────────────────────
    private String adjustLevelByMealProportion(String base, MealType type, Integer mealCalories) {
        // 끼니별 기준 칼로리
        int expected = switch (type) {
            case BREAKFAST -> 550;
            case LUNCH -> 770;
            case DINNER -> 880;
            default -> 700;
        };


        double ratio = (double) mealCalories / expected;
        if (ratio < 0.5) return "적게";
        else if (ratio > 1.5) return "많이";
        else return base;
    }

    private String getMealName(MealType mealType) {
        return switch (mealType) {
            case BREAKFAST -> "아침";
            case LUNCH -> "점심";
            case DINNER -> "저녁";
            default -> "기타";
        };
    }

    //기록-오늘먹은음식조회
    @Override
    public FoodHistoryTodayResponse getTodayFoodHistory(Long userId, LocalDate date) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate targetDate = (date != null) ? date : LocalDate.now(kst);

        // KST 00:00~24:00을 UTC 시간으로 변환
        LocalDateTime startKst = targetDate.atStartOfDay(kst).toLocalDateTime();
        LocalDateTime endKst   = startKst.plusDays(1);

        log.info("[오늘의 음식 조회] userId={}, KST일자={}, 범위(KST)={}~{}",
                userId, targetDate, startKst, endKst);


        List<Object[]> result = historyRepository.findTodayFoods(userId, startKst, endKst);

        if (result.isEmpty()) {
            log.warn("오늘({}) 식단 기록 없음 (userId={})", targetDate, userId);
            return null;
        }

        Map<MealType, List<FoodHistoryTodayResponse.FoodItemDto>> grouped =
                result.stream()
                        .collect(Collectors.groupingBy(
                                row -> (MealType) row[0],
                                LinkedHashMap::new,
                                Collectors.mapping(row -> FoodHistoryTodayResponse.FoodItemDto.builder()
                                                .foodId((Long) row[1])
                                                .name((String) row[2])
                                                .calories(((Number) row[3]).intValue())
                                                .imageUrl((String) row[4])
                                                .build(),
                                        Collectors.toList())
                        ));

        return FoodHistoryTodayResponse.builder()
                .date(targetDate)
                .meal_type(grouped)
                .build();
    }

}
