package com.cloudbread.domain.feedback.dto;

import com.cloudbread.domain.nutrition.domain.entity.UserDailyNutrition;
import com.cloudbread.domain.feedback.dto.UserFeedbackRequestDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ FastAPI 피드백 API 호출용 Request DTO
 *    구조는 FastAPI pydantic 모델과 완전히 동일하게 매핑
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequestDto {

    private UserFeedbackRequestDto.AiUserRequest user;
    private NutritionBalance balance;

    /**
     * ✅ 영양 밸런스 데이터 포함 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionBalance {
        private String date;
        private Map<String, NutrientValue> balance;
    }

    /**
     * ✅ 영양소 단위 값
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutrientValue {
        private BigDecimal actual;
        private BigDecimal recommended;
        private String unit;
    }

    /**
     * ✅ Factory Method
     *    - user: FastAPI user section
     *    - dailyNutritions: 오늘의 영양 데이터 리스트
     */
    public static FeedbackRequestDto of(
            UserFeedbackRequestDto.AiUserRequest user,
            List<UserDailyNutrition> dailyNutritions
    ) {
        if (dailyNutritions == null || dailyNutritions.isEmpty()) {
            throw new IllegalStateException("오늘의 영양 밸런스 데이터가 없습니다. userId="
                    + (user != null ? user.getBirthDate() : "unknown"));
        }

        // ✅ FastAPI 명세에 맞게 key 이름 단수형으로 매핑
        Map<String, NutrientValue> balanceMap = new HashMap<>();
        for (UserDailyNutrition n : dailyNutritions) {
            String key;
            switch (n.getNutrient().name()) {
                case "CARBS" -> key = "carbs";
                case "PROTEINS" -> key = "protein";
                case "FATS" -> key = "fat";
                default -> key = n.getNutrient().name().toLowerCase();
            }

            balanceMap.put(key, new NutrientValue(
                    n.getActual(),
                    n.getRecommended(),
                    n.getUnit()
            ));
        }

        LocalDate date = dailyNutritions.get(0).getDate();

        return FeedbackRequestDto.builder()
                .user(user)
                .balance(
                        NutritionBalance.builder()
                                .date(date.toString())
                                .balance(balanceMap)
                                .build()
                )
                .build();
    }
}
