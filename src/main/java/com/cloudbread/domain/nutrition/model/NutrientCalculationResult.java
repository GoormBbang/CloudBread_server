//package com.cloudbread.domain.nutrition.model;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//
//@Getter
//@AllArgsConstructor
//public class NutrientCalculationResult {
//    private double folicAcid;
//    private double calcium;
//    private double iron;
//}
package com.cloudbread.domain.nutrition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 영양 계산 결과 모델
 * - case1: 오늘의 영양 섭취량 계산용 (엽산, 칼슘, 철분)
 * - case2: 부족 영양소 계산용 (부족한 영양소명 + 부족량)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NutrientCalculationResult {

    // case1
    private double folicAcid;
    private double calcium;
    private double iron;

    // case2
    private String deficientNutrient;
    private double deficientValue;

    // ✅ case1 생성자 (엽산/칼슘/철분용)
    public NutrientCalculationResult(double folicAcid, double calcium, double iron) {
        this.folicAcid = folicAcid;
        this.calcium = calcium;
        this.iron = iron;
    }

    // ✅ case2 생성자 (부족 영양소용)
    public NutrientCalculationResult(String deficientNutrient, double deficientValue) {
        this.deficientNutrient = deficientNutrient;
        this.deficientValue = deficientValue;
    }
}
