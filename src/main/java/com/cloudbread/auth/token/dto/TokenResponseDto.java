package com.cloudbread.auth.token.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticationResponse {
        private String accessToken;
        private String refreshToken;

    }

}
