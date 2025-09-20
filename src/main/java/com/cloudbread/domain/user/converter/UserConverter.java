package com.cloudbread.domain.user.converter;

import com.cloudbread.domain.user.domain.entity.Allergy;
import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.dto.UserResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static UserResponseDto.Example toExample(User user){
        return UserResponseDto.Example.builder()
                .id(user.getId())
                .nickname(user.getNickname())
         //       .age(user.getAge())
         //       .isAdult(user.isAdult())
                .build();
    }

    public static UserResponseDto.UpdateResponse toUpdateResponse(User user){
        return UserResponseDto.UpdateResponse.builder()
                .id(user.getId())
                .build();
    }

    public static List<UserResponseDto.MetadataItemDto> toDietTypeDtoList(List<DietType> dietTypes) {
        return dietTypes.stream()
                .map(dietType -> UserResponseDto.MetadataItemDto.builder()
                        .id(dietType.getId())
                        .name(dietType.getName().name()) // Enum 타입은 .name()으로 String 변환
                        .build())
                .collect(Collectors.toList());
    }

    public static List<UserResponseDto.MetadataItemDto> toHealthTypeDtoList(List<HealthType> healthTypes) {
        return healthTypes.stream()
                .map(healthType -> UserResponseDto.MetadataItemDto.builder()
                        .id(healthType.getId())
                        .name(healthType.getName().name())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<UserResponseDto.MetadataItemDto> toAllergyDtoList(List<Allergy> allergies) {
        return allergies.stream()
                .map(allergy -> UserResponseDto.MetadataItemDto.builder()
                        .id(allergy.getId())
                        .name(allergy.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public static UserResponseDto.MyInfoResponse toMyInfoResponse(
            User user,
            List<UserResponseDto.MetadataItemDto> dietTypes,
            List<UserResponseDto.MetadataItemDto> healthTypes,
            List<UserResponseDto.MetadataItemDto> allergies
    ) {
        return UserResponseDto.MyInfoResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .height(user.getHeight() != null ? user.getHeight().doubleValue() : null)
                .weight(user.getWeight() != null ? user.getWeight().doubleValue() : null)
                .dietTypes(dietTypes)
                .healthTypes(healthTypes)
                .allergies(allergies)
                .other_health_factors(user.getOtherHealthFactors())
                .build();
    }




}
