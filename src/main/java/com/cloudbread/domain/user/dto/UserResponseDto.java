package com.cloudbread.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

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
    @NoArgsConstructor // 역직렬화를 위해 기본 생성자 추가
    @Getter
    public static class MetadataResponse {
        private List<MetadataItemDto> dietTypes;
        private List<MetadataItemDto> healthTypes;
        private List<MetadataItemDto> allergies;
    }


    @Builder
    @AllArgsConstructor
    @NoArgsConstructor // 역직렬화를 위해 기본 생성자 추가
    @Getter
    public static class MetadataItemDto {
        private Long id;
        private String name;
    }

}
