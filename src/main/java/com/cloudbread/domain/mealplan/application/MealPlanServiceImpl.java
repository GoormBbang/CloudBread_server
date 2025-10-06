package com.cloudbread.domain.mealplan.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.mealplan.client.FastApiMealPlanClient;
import com.cloudbread.domain.mealplan.domain.entity.MealPlan;
import com.cloudbread.domain.mealplan.domain.entity.MealPlanItem;
import com.cloudbread.domain.mealplan.domain.repository.MealPlanItemRepository;
import com.cloudbread.domain.mealplan.domain.repository.MealPlanRepository;
import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.enums.MealType;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.user.dto.UserRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanServiceImpl implements MealPlanService {

    private final FastApiMealPlanClient fastApiMealPlanClient;
    private final UserRepository userRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserDietRepository userDietRepository;
    private final UserFoodHistoryRepository userFoodHistoryRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealPlanItemRepository mealPlanItemRepository; // ✅ 추가
    private final FoodRepository foodRepository; // ✅ 추가

    @Override
    public MealPlanResponseDto refreshMealPlan(Long userId) {
        // ✅ 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        // ✅ 2~4. 건강/알레르기/식단 정보 수집
        List<String> healths = userHealthRepository.findByUserId(userId)
                .stream()
                .map(h -> h.getHealthType().getName().name())
                .collect(Collectors.toList());

        List<String> allergies = userAllergyRepository.findByUserId(userId)
                .stream()
                .map(a -> a.getAllergy().getName())
                .collect(Collectors.toList());

        List<String> diets = userDietRepository.findByUserId(userId)
                .stream()
                .map(d -> d.getDietType().getName().name())
                .collect(Collectors.toList());

        // ✅ 5. 최근 음식 기록
        List<UserRequestDto.FoodHistoryDto> foodHistory = userFoodHistoryRepository
                .findRecentByUserId(userId, LocalDate.now().minusDays(1).atStartOfDay())
                .stream()
                .map(UserRequestDto.FoodHistoryDto::fromEntity)
                .collect(Collectors.toList());

        // ✅ 6. 요청 DTO 생성
        UserRequestDto.AiUserRequest aiUserRequest = UserRequestDto.AiUserRequest.from(user, foodHistory);
        MealPlanRequestDto requestDto = MealPlanRequestDto.of(aiUserRequest, healths, allergies, diets);

        // ✅ 7. FastAPI 호출
        MealPlanResponseDto response = fastApiMealPlanClient.requestMealPlan(requestDto);

        MealPlan mealPlan = MealPlan.builder()
                .user(user)
                .planDate(LocalDate.parse(response.getPlanDate()))
                .reasonDesc(null)
                .build();

        // ✅ 기존 변수 재사용 (중복 선언 X)
        mealPlan = mealPlanRepository.save(mealPlan);
        final MealPlan savedMealPlan = mealPlan; // 🔒 Lambda에서 사용하려면 final 참조로 한번 감싸기

        List<MealPlanItem> items = new ArrayList<>();

        response.getSections().forEach(section -> {
            MealType type = safeMealType(section.getMealType());
            section.getItems().forEach(itemDto -> {

                Food foodRef = foodRepository.findById(itemDto.getFoodId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식 ID: " + itemDto.getFoodId()));

                MealPlanItem item = MealPlanItem.builder()
                        .mealPlan(savedMealPlan)
                        .food(foodRef)
                        .mealType(type)
                        .estCalories(itemDto.getEstCalories())        // ✅ 한 음식당 칼로리
                        .portionLabel(itemDto.getPortionLabel())      // ✅ 음식 기준 양
                        .category(itemDto.getFoodCategory())          // ✅ 음식 카테고리
                        .build();

                items.add(item);

            });
        });

        mealPlanItemRepository.saveAll(items);
        return response;
    }

    private MealType safeMealType(String value) {
        try {
            return MealType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return MealType.ETC;
        }
    }
}
