package com.cloudbread.domain.mealplan.dto;

import com.cloudbread.domain.user.dto.UserRequestDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanRequestDto {

    private UserRequestDto.AiUserRequest user;
    private List<String> healths;
    private List<String> allergies;
    private List<String> diets;

    public static MealPlanRequestDto of(
            UserRequestDto.AiUserRequest user,
            List<String> healths,
            List<String> allergies,
            List<String> diets
    ) {
        return MealPlanRequestDto.builder()
                .user(user)
                .healths(healths)
                .allergies(allergies)
                .diets(diets)
                .build();
    }
}
