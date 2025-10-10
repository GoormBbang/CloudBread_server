package com.cloudbread.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 탄단지(3대 영양소) 밸런스 응답 DTO - 영양 밸런스 api용
 * - 각 영양소의 실제 섭취량(actual), 권장 섭취량(recommended), 단위(unit) 포함
 */
@Getter
@Builder
public class NutritionBalanceResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Map<String, NutrientInfo> balance;

    @Getter
    public static class NutrientInfo {
        private final BigDecimal actual;   // 실제 섭취량
        private final double recommended;  // 권장 섭취량
        private final String unit;         // 단위 (g)

        public NutrientInfo(BigDecimal actual, double recommended, String unit) {
            this.actual = actual;
            this.recommended = recommended;
            this.unit = unit;
        }
    }

    public static class NutritionBalanceResponseBuilder {
        private Map<String, NutrientInfo> balance = new HashMap<>();

        public NutritionBalanceResponseBuilder carbs(BigDecimal actual, double recommended) {
            balance.put("carbs", new NutrientInfo(actual, recommended, "g"));
            return this;
        }

        public NutritionBalanceResponseBuilder protein(BigDecimal actual, double recommended) {
            balance.put("protein", new NutrientInfo(actual, recommended, "g"));
            return this;
        }

        public NutritionBalanceResponseBuilder fat(BigDecimal actual, double recommended) {
            balance.put("fat", new NutrientInfo(actual, recommended, "g"));
            return this;
        }
    }
}
