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
            map.put(key, PhotoAnalysisResponse.NutrientValue.builder()
                    .value(fn.getValue())
                    .unit(unit)
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
}
