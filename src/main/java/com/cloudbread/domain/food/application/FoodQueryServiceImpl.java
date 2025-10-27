package com.cloudbread.domain.food.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.food.dto.FoodResponse;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodQueryServiceImpl implements FoodQueryService {
    private final FoodRepository foodRepository;
    private final FoodNutrientRepository foodNutrientRepository;


    // 영양소 영문키(소문자) → 한국어명 매핑
    private static final Map<String, String> KNAME_MAP = Map.ofEntries(
            Map.entry("carbs",          "탄수화물"),
            Map.entry("proteins",       "단백질"),
            Map.entry("fats",           "지방"),
            Map.entry("sugars",         "당류"),
            Map.entry("saturated_fat",  "포화지방"),
            Map.entry("trans_fat",      "트랜스지방"),
            Map.entry("cholesterol",    "콜레스테롤"),
            Map.entry("sodium",         "나트륨"),
            Map.entry("folic_acid",     "엽산"),
            Map.entry("iron",           "철"),
            Map.entry("calcium",        "칼슘"),
            Map.entry("moisture",       "수분")
    );
    @Override
    @Transactional(readOnly = true)
    public List<FoodResponse.SuggestItem> suggest(String q, int limit) {
        var page = PageRequest.of(0, Math.max(1, limit));
        var foods = foodRepository.searchByNameContainsSuggest(q, page);

        return foods.stream()
                .map(f -> new FoodResponse.SuggestItem(
                        f.getId(), f.getName(), f.getCategory(), f.getCalories()
                ))
                .toList();
    }

    @Override
    public PhotoAnalysisResponse.ConfirmSelected getFoodDetail(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("food not found: " + foodId));

        BigDecimal calories = food.getCalories();
        Map<String, PhotoAnalysisResponse.NutrientValue> nutrients = buildNutrientsWithoutCalories(food);

        return PhotoAnalysisResponse.ConfirmSelected.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .serving(food.getSourceName())
                .calories(calories)
                .nutrients(nutrients)
                .build();
    }


    private Map<String, PhotoAnalysisResponse.NutrientValue> buildNutrientsWithoutCalories(Food food) {
        List<FoodNutrient> list = foodNutrientRepository.findByFoodId(food.getId());
        Map<String, PhotoAnalysisResponse.NutrientValue> map = new LinkedHashMap<>();

        for (FoodNutrient fn : list) {
            String key = normalizeKey(fn.getNutrient().getName()); // 예: "carbs"
            if ("calories".equals(key)) continue; // 칼로리는 별도 필드

            String unit = unitSymbol(fn.getNutrient().getUnit());  // "g" | "mg" | "μg" 등
            String kname = knameFor(key);                           // 한국어명

            map.put(key, PhotoAnalysisResponse.NutrientValue.builder()
                    .value(fn.getValue())
                    .unit(unit)
                    .kname(kname)
                    .build());
        }
        return map;
    }

    private String normalizeKey(String s) {
        return s == null ? null : s.toLowerCase();
    }

    private String unitSymbol(Unit u) {
        return (u == null) ? null : u.name().toLowerCase();
    }

    private String knameFor(String key) {
        if (key == null) return null;
        return KNAME_MAP.getOrDefault(key, key); // 매핑 없으면 원문 그대로(안전)
    }
}
