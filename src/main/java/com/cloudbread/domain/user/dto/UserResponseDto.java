package com.cloudbread.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

}
