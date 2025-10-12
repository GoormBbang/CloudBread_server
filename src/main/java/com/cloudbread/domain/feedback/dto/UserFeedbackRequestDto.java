package com.cloudbread.domain.feedback.dto;

import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class UserFeedbackRequestDto {

    // ✅ FastAPI 피드백용 유저 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiUserRequest {
        private String birthDate;              // 생년월일
        private String dueDate;                // 출산 예정일
        private List<String> otherHealthFactors; // ✅ FastAPI는 리스트 기대
        private List<FoodHistoryDto> foodHistory; // 최근 음식 기록

        private List<String> healths;   // 건강 상태
        private List<String> allergies; // 알러지
        private List<String> diets;     // 식단 유형

        public static AiUserRequest from(
                User user,
                List<FoodHistoryDto> foodHistory,
                List<String> healths,
                List<String> allergies,
                List<String> diets
        ) {
            return AiUserRequest.builder()
                    .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : "")
                    .dueDate(user.getDueDate() != null ? user.getDueDate().toString() : "")
                    .otherHealthFactors(
                            user.getOtherHealthFactors() != null
                                    ? List.of(user.getOtherHealthFactors()) // ✅ 단일 문자열을 리스트로 감싸기
                                    : List.of()
                    )
                    .healths(healths)
                    .allergies(allergies)
                    .diets(diets)
                    .foodHistory(foodHistory)
                    .build();
        }
    }

    // ✅ 피드백 API용 음식 기록 DTO
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodHistoryDto {
        private String mealType;   // 식사 타입 (BREAKFAST/LUNCH/DINNER)
        private String foodName;   // ✅ FastAPI에서 필수
        private int intakePercent; // 섭취량 (0~100)

        // ✅ ISO 문자열로 직렬화 ("2025-10-12T08:30:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static FoodHistoryDto fromEntity(UserFoodHistory entity) {
            return new FoodHistoryDto(
                    entity.getMealType().name(),
                    entity.getFood().getName(),  // ✅ foodName 반드시 포함
                    entity.getIntakePercent(),
                    entity.getCreatedAt()
            );
        }
    }
}

