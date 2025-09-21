package com.cloudbread.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDate;
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

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UpdateUserSummaryRequest {//메인-내 정보 수정
        private String nickname;
        private LocalDate birthDate; // 생년월일
    }


}

