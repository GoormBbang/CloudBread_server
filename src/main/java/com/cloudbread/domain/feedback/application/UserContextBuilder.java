package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.feedback.dto.UserFeedbackRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserContextBuilder {

    private final UserRepository userRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserDietRepository userDietRepository;
    private final UserFoodHistoryRepository userFoodHistoryRepository;

    /**
     * ✅ FastAPI 피드백용 유저 컨텍스트 구성
     */
    public UserFeedbackRequestDto.AiUserRequest buildFeedbackUserRequest(Long userId) {
        // 1️⃣ 유저 기본 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID=" + userId));

        // 2️⃣ 건강 상태, 알러지, 식단 타입
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

        // 3️⃣ 최근 음식 기록 (오늘 포함)
        var foodHistory = userFoodHistoryRepository
                .findRecentByUserId(userId, LocalDate.now().minusDays(1).atStartOfDay())
                .stream()
                .map(UserFeedbackRequestDto.FoodHistoryDto::fromEntity) // ✅ 변경된 DTO 사용
                .collect(Collectors.toList());

        // 4️⃣ FastAPI 피드백용 DTO 생성
        return UserFeedbackRequestDto.AiUserRequest.from(
                user,
                foodHistory,
                healths,
                allergies,
                diets
        );
    }
}
