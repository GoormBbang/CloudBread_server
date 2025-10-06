package com.cloudbread.domain.user.dto;

import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.entity.UserFoodHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserRequestDto {

    // 회원가입 STEP2
    @Builder
    @AllArgsConstructor
    @Getter
    public static class UpdateDetailsRequest {
        private LocalDate birthDate; // 생년월일
        private BigDecimal weight;
        private BigDecimal height;
        private LocalDate dueDate; // 임신시작일
    }


    // 회원가입 STEP 3
    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class UpdateHealthInfoRequest {
        private List<Long> dietTypeIds;
        private List<Long> healthTypeIds;
        private List<Long> allergyIds;
        private String otherHealthFactors;
    }

    //설정화면 - 내 정보 수정
    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class UpdateMyInfoRequest {
        private String nickname;//이름
        private LocalDate dueDate;//임신시작일
        private BigDecimal height;
        private BigDecimal weight;
        private List<Long> dietTypeIds;
        private List<Long> healthTypeIds;
        private List<Long> allergyIds;
        private String otherHealthFactors;
    }

    //메인-내 정보 수정
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UpdateUserSummaryRequest {
        private String nickname;
        private LocalDate birthDate; // 생년월일
    }

    // ✅ AI 추천 식단 요청용 유저 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiUserRequest {
        //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private String birthDate;              // 생년월일
        //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private String dueDate;                // 출산 예정일
        private String otherHealthFactors;        // 기타 건강 특성
        private List<FoodHistoryDto> foodHistory; // 최근 음식 기록

        public static AiUserRequest from(User user, List<FoodHistoryDto> foodHistory) {
            return AiUserRequest.builder()
                    .birthDate(
                            user.getBirthDate() != null
                                    ? user.getBirthDate().toString()
                                    : ""  // null 방지
                    )
                    .dueDate(
                            user.getDueDate() != null
                                    ? user.getDueDate().toString()
                                    : ""  // null 방지
                    )
                    .otherHealthFactors(user.getOtherHealthFactors())
                    .foodHistory(foodHistory)
                    .build();
        }
    }


        // ✅ 음식 히스토리 DTO
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodHistoryDto {
        private String mealType;//식단타입(아/점/저)
        private int intakePercent;//섭취량
        private LocalDateTime createdAt;//섭취시간
        //photo_analysis_id 추가(나중에)

        public static FoodHistoryDto fromEntity(UserFoodHistory entity) {
            return new FoodHistoryDto(
                    entity.getMealType().name(),
                    entity.getIntakePercent(),
                    entity.getCreatedAt()
            );
        }
    }

}

