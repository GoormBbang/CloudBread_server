package com.cloudbread.domain.mealplan.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.mealplan.client.FastApiMealPlanClient;
import com.cloudbread.domain.mealplan.converter.MealPlanConverter;
import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import com.cloudbread.domain.mealplan.domain.entity.MealPlanItem;
import com.cloudbread.domain.mealplan.domain.repository.MealPlanRepository;
import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.enums.MealType;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.nutrition.application.UserNutritionStatsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MealPlanServiceImpl implements MealPlanService {

    private final FastApiMealPlanClient fastApiMealPlanClient;
    private final UserRepository userRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserDietRepository userDietRepository;
    private final UserFoodHistoryRepository userFoodHistoryRepository;
    private final MealPlanRepository mealPlanRepository;
    private final FoodRepository foodRepository;
    private final MealPlanConverter mealPlanConverter;
    private final UserNutritionStatsService userNutritionStatsService;

    @Override
    public MealPlanResponseDto refreshMealPlan(Long userId) {

        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        // 2. 건강/알러지/식단 정보 조회
        List<String> healths = userHealthRepository.findByUserId(userId)
                .stream().map(h -> h.getHealthType().getName().name()).collect(Collectors.toList());

        List<String> allergies = userAllergyRepository.findByUserId(userId)
                .stream().map(a -> a.getAllergy().getName()).collect(Collectors.toList());

        List<String> diets = userDietRepository.findByUserId(userId)
                .stream().map(d -> d.getDietType().getName().name()).collect(Collectors.toList());

        // 3. 최근 음식 기록 조회
        List<UserRequestDto.FoodHistoryDto> foodHistory = userFoodHistoryRepository
                .findRecentByUserId(userId, LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).atStartOfDay())
                .stream().map(UserRequestDto.FoodHistoryDto::fromEntity).collect(Collectors.toList());

        // 4. FastAPI 요청 생성 및 호출
        UserRequestDto.AiUserRequest aiUserRequest = UserRequestDto.AiUserRequest.from(user, foodHistory);
        MealPlanRequestDto requestDto = MealPlanRequestDto.of(aiUserRequest, healths, allergies, diets);

        MealPlanResponseDto aiResponse = fastApiMealPlanClient.requestMealPlan(requestDto);

        // 5. 날짜 처리 (KST 기준)
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate planDate = LocalDate.now(KST);

//        String planDateStr = aiResponse.getPlanDate();
//        LocalDate planDate = (planDateStr != null && !planDateStr.isBlank())
//                ? LocalDate.parse(planDateStr)
//                : LocalDate.now(ZoneId.of("Asia/Seoul")); // 한국 시간 기준으로 수정
        log.info("ai 추천 식단, planDate={}", planDate);

        // 6. MealPlan 엔티티 생성
        MealPlan mealPlan = MealPlan.builder()
                .user(user)
                .planDate(planDate)
                .reasonDesc(null)
                .build();

        // 7. DB 저장용 item 변환 (FastAPI 원본 그대로 DB에만 저장)
        if (aiResponse.getSections() != null) {
            for (MealPlanResponseDto.SectionDto section : aiResponse.getSections()) {
                for (MealPlanResponseDto.FoodItemDto itemDto : section.getItems()) {

                    Food foodRef = foodRepository.getReferenceById(itemDto.getFoodId());

                    MealPlanItem item = MealPlanItem.builder()
                            .food(foodRef)
                            .mealType(safeMealType(section.getMealType()))
                            .foodName(itemDto.getName())
                            .portionLabel(itemDto.getPortionLabel())
                            .estCalories(itemDto.getEstCalories())
                            .foodCategory(itemDto.getFoodCategory())
                            .build();

                    mealPlan.addMealPlanItem(item);
                }
            }
        }

        // 8. CascadeType.ALL 덕분에 item 자동 저장
        mealPlanRepository.save(mealPlan);

        // 9. 영양 밸런스 즉시 계산 후 DB 반영 (KST 기준)
        userNutritionStatsService.getNutritionBalance(userId, planDate);

        // 10. FastAPI 원본 그대로 리턴
        return aiResponse;
    }

    private MealType safeMealType(String value) {
        try {
            return MealType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return MealType.ETC;
        }
    }

    /**
     * 오늘(KST) 식단 조회
     */
    @Override
    @Transactional(readOnly = true)
    public MealPlanResponseDto getTodayMealPlan(Long userId) {
        LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("todayKst={}", todayKst);

        MealPlan mealPlan = mealPlanRepository
                .findOneWithItemsByUserIdAndPlanDate(userId, todayKst)
                .orElseThrow(() -> new NoSuchElementException("오늘 날짜의 식단이 없습니다."));

        return toResponseDto(mealPlan);
    }

    /**
     * 엔티티 → 응답 DTO 즉시 변환 (섹션 그룹화 + kcal 합산)
     */
    private MealPlanResponseDto toResponseDto(MealPlan mealPlan) {
        Map<MealType, List<MealPlanItem>> byType = mealPlan.getMealPlanItems().stream()
                .collect(Collectors.groupingBy(MealPlanItem::getMealType, LinkedHashMap::new, Collectors.toList()));

        List<MealPlanResponseDto.SectionDto> sections = new ArrayList<>();

        for (Map.Entry<MealType, List<MealPlanItem>> entry : byType.entrySet()) {
            List<MealPlanItem> items = entry.getValue();

            int totalKcal = items.stream()
                    .mapToInt(MealPlanItem::getEstCalories)
                    .sum();

            List<MealPlanResponseDto.FoodItemDto> itemDtos = items.stream()
                    .map(i -> new MealPlanResponseDto.FoodItemDto(
                            i.getFood() != null ? i.getFood().getId() : null,
                            i.getFoodName(),
                            i.getPortionLabel(),
                            i.getEstCalories(),
                            i.getFoodCategory()
                    ))
                    .collect(Collectors.toList());

            sections.add(new MealPlanResponseDto.SectionDto(
                    (entry.getKey() != null ? entry.getKey() : MealType.ETC).name(),
                    totalKcal,
                    itemDtos
            ));
        }

        MealPlanResponseDto dto = new MealPlanResponseDto();
        dto.setPlanId(mealPlan.getId());
        dto.setPlanDate(mealPlan.getPlanDate() != null ? mealPlan.getPlanDate().toString() : null);
        dto.setSections(sections);
        return dto;
    }
}
