package com.cloudbread.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

}

