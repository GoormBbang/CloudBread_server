package com.cloudbread.domain.user.converter;

import com.cloudbread.domain.user.domain.entity.Allergy;
import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.dto.UserResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.LocalDate;


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

    //로그인한 사용자 정보 조회
//    public static UserResponseDto.UserSummaryResponse toUserSummaryResponse(User user) {
//        return UserResponseDto.UserSummaryResponse.builder()
//                .id(user.getId())
//                .nickname(user.getNickname())
//                .profileImageUrl(user.getProfileImageUrl())
//                .dueDate(user.getDueDate())
//                .build();
//    }
    public static UserResponseDto.UserSummaryResponse toUserSummaryResponse(User user) {
        LocalDate dueDate = user.getDueDate();

        // ✅ 임신 주차 계산
        Integer pregnancyWeek = null;
        if (dueDate != null) {
            ZoneId KST = ZoneId.of("Asia/Seoul");
            LocalDate today = LocalDate.now(KST);
            // 임신은 보통 40주를 기준으로 계산
            LocalDate startDate = dueDate.minusWeeks(40);
            long weeks = ChronoUnit.WEEKS.between(startDate, today);

            // 0 이하 방어
            pregnancyWeek = (int) Math.max(weeks, 0);
        }

        return UserResponseDto.UserSummaryResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .dueDate(dueDate)
                .pregnancyWeek(pregnancyWeek)
                .build();
    }


}
