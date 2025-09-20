package com.cloudbread.global.db_seeder.food;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 엑셀데이터를 파싱하여, API에 전달할 DTO
public class FoodSeedDto {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportResultDto {
        private int totalRows;       // 읽은 데이터 행수(헤더 제외)
        private int foodsUpserted;   // Food 신규/수정 건수
        private int nutrientsLinked; // FoodNutrient insert/업데이트 건수
        private java.util.List<String> warnings; // 파싱 실패/누락 로그
    }
}
