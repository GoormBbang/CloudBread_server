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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
                .findRecentByUserId(userId, LocalDate.now().minusDays(1).atStartOfDay())
                .stream().map(UserRequestDto.FoodHistoryDto::fromEntity).collect(Collectors.toList());

        // 4. FastAPI 요청 생성 및 호출
        UserRequestDto.AiUserRequest aiUserRequest = UserRequestDto.AiUserRequest.from(user, foodHistory);
        MealPlanRequestDto requestDto = MealPlanRequestDto.of(aiUserRequest, healths, allergies, diets);

        MealPlanResponseDto aiResponse = fastApiMealPlanClient.requestMealPlan(requestDto);

        // 5. 날짜 처리
        String planDateStr = aiResponse.getPlanDate();
        LocalDate planDate = (planDateStr != null && !planDateStr.isBlank())
                ? LocalDate.parse(planDateStr)
                : LocalDate.now();

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

        // 9. FastAPI 원본 그대로 리턴
        return aiResponse;
    }

    private MealType safeMealType(String value) {
        try {
            return MealType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return MealType.ETC;
        }
    }
}
