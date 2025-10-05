package com.cloudbread.domain.photo_analyses.application.es;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/*
    ES가 아직 없어도, 이름 포함 검색 + 간단 점수로 3개 뽑는 더미 구현
    ES는 이후에 ES를 붙여서 인터페이스 구현 갈아끼울 것
 */
@Component
@RequiredArgsConstructor
public class JpaCandidateFinder implements CandidateFinder {
    private final FoodRepository foodRepository;
    private final FoodNutrientRepository foodNutrientRepository;

    @Override
    public List<PhotoAnalysisResponse.CandidateItem> find(String query, int limit) {
        // 1) 후보 풀: 이름 LIKE 검색 (대소문자 무시)
        List<Food> pool = foodRepository.searchByNameContains(query, 30); // 아래 Repo 메서드 추가

        // 2) 간단 점수: 포함여부 + 길이 유사도
        return pool.stream()
                .map(f -> {
                    double contains = f.getName().toLowerCase().contains(query.toLowerCase()) ? 1.0 : 0.0; // 음식이름이 검색어에 포함되면 1.0, 아니면 0.0
                    double lenSim = (double) Math.min(f.getName().length(), query.length()) / Math.max(f.getName().length(), query.length()); // 길이 유사도
                    double score = contains * 0.7 + lenSim * 0.3; // 최종 스코어 (이름에 70% 가중치를, lenSim 점수에 30% 가중치를)

                    Map<String, PhotoAnalysisResponse.NutrientValue> nutrients = toNutrientsMap(f);
                    return PhotoAnalysisResponse.CandidateItem.builder()
                            .foodId(f.getId())
                            .name(f.getName())
                            .score(score)
                            .calories(f.getCalories())
                            .nutrients(nutrients)
                            .build();
                })
                .sorted(Comparator.comparingDouble(PhotoAnalysisResponse.CandidateItem::getScore).reversed()) // 점수순 내림차순
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<String, PhotoAnalysisResponse.NutrientValue> toNutrientsMap(Food food) {
        List<FoodNutrient> list = foodNutrientRepository.findByFoodId(food.getId());

        Map<String, PhotoAnalysisResponse.NutrientValue> map = new LinkedHashMap<>();
        for (FoodNutrient fn : list) {
            String key  = normalizeKey(fn.getNutrient().getName()); // 예: "carbs"
            String unit = unitSymbol(fn.getNutrient().getUnit());   // "g" | "mg" | "μg" ...

            if ("calories".equals(key)) continue;

            map.put(key, PhotoAnalysisResponse.NutrientValue.builder()
                    .value(fn.getValue())
                    .unit(unit)
                    .build());
        }

        return map;
    }

    private String normalizeKey(String s) { return s == null ? null : s.toLowerCase(); }

    private String unitSymbol(Unit u) {
        if (u == null) return null;
        // enum 이름 대소문자 어떤 걸 쓰든 안전하게 문자열로
        return u.name().toLowerCase(); // 예: G -> "g", MG -> "mg", UG -> "ug"
    }

//    private Map<String, Object> toNutrientsMap(Long foodId) {
//        List<FoodNutrient> list = foodNutrientRepository.findByFoodId(foodId);
//
//        Map<String, Object> map = new LinkedHashMap<>();
//        list.forEach(fn -> {
//            String key = fn.getNutrient().getName().toLowerCase(); // 예: "CARBS", "SODIUM" 등
//            map.put(key, fn.getValue());
//        });
//        return map;
//    }
}
