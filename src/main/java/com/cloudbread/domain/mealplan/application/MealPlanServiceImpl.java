package com.cloudbread.domain.mealplan.application;

import com.cloudbread.domain.mealplan.client.FastApiMealPlanClient;
import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.user.dto.UserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Override
    public MealPlanResponseDto refreshMealPlan(Long userId) {
        // ✅ 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // ✅ 2. 건강 상태
        List<String> healths = userHealthRepository.findByUserId(userId)
                .stream()
                .map(h -> h.getHealthType().getName().name())
                .collect(Collectors.toList());

        // ✅ 3. 알레르기
        List<String> allergies = userAllergyRepository.findByUserId(userId)
                .stream()
                .map(a -> a.getAllergy().getName())
                .collect(Collectors.toList());

        // ✅ 4. 식단 취향
        List<String> diets = userDietRepository.findByUserId(userId)
                .stream()
                .map(d -> d.getDietType().getName().name())
                .collect(Collectors.toList());

        // ✅ 5. 어제 먹은 음식 기록
        List<UserRequestDto.FoodHistoryDto> foodHistory = userFoodHistoryRepository
                .findRecentByUserId(userId, LocalDate.now().minusDays(1).atStartOfDay())
                .stream()
                .map(UserRequestDto.FoodHistoryDto::fromEntity)
                .collect(Collectors.toList());

        // ✅ 6. 유저 DTO 조립
        UserRequestDto.AiUserRequest userDto = UserRequestDto.AiUserRequest.from(user, foodHistory);

        // ✅ 7. FastAPI 요청 DTO 생성
        MealPlanRequestDto requestDto = MealPlanRequestDto.of(userDto, healths, allergies, diets);

        // ✅ 8. FastAPI 호출 (동기)
        MealPlanResponseDto response = fastApiMealPlanClient.requestMealPlan(requestDto);

        // ✅ 9. 필요하면 DB 저장 로직 추가
        // saveMealPlan(response, user);

        return response;
    }
}
