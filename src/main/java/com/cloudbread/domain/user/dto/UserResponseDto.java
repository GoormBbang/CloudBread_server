package com.cloudbread.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

public class UserResponseDto {

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Example {
        private Long id;
        private String nickname;
        private int age;
        private boolean isAdult;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class SecurityContextDto {
        private Long id;
        private String email;
    }

    // 회원가입 STEP2
    @Builder
    @AllArgsConstructor
    @Getter
    public static class UpdateResponse {
        private Long id; // 업데이트된 userId
    }

    // 메타데이터 (한번에)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MetadataResponse {
        private List<MetadataItemDto> dietTypes;
        private List<MetadataItemDto> healthTypes;
        private List<MetadataItemDto> allergies;
    }


    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MetadataItemDto {
        private Long id;
        private String name;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MyInfoResponse {//내 정보 조회
        private Long id;
        private String nickname;
        private Double height;
        private Double weight;
        private List<MetadataItemDto> dietTypes;
        private List<MetadataItemDto> healthTypes;
        private List<MetadataItemDto> allergies;
        private String other_health_factors;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class UpdateRespnse{//설정화면-내 정보 수정
        private Long userid;
        private String massage;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class UserSummaryResponse {//메인-로그인한 사용자 정보 조회
        private Long id;
        private String nickname;
        private String profileImageUrl;
        private LocalDate dueDate;
        private Integer pregnancyWeek;//임신주차

    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class UpdateUserSummaryResponse {// 메인-내 정보 수정
        private Long id;
        private LocalDate birthDate;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class TipResponse {//이번주차팁 전체조회
        private int week_number;
        private List<TipDto> tips;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class TipDto {//이번주차팁-태아,임산부,영양 조회시 사용
        private Long id;
        private String kind;      // BABY, MOM, NUTRITION
        private String title;
        private String description;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileResponse {
        private String nickname;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate birthDate;
    }
}
