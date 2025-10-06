package com.cloudbread.domain.food_history.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.food_history.dto.FoodHistoryRequest;
import com.cloudbread.domain.food_history.dto.FoodHistoryResponse;
import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import com.cloudbread.domain.photo_analyses.domain.repository.PhotoAnalysisRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.cloudbread.domain.user.domain.repository.UserFoodHistoryRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class FoodHistoryServiceImpl implements FoodHistoryService {
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final UserFoodHistoryRepository foodHistoryRepository;
    private final PhotoAnalysisRepository photoAnalysisRepository;

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

}
