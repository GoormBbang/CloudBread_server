package com.cloudbread.domain.food.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.food.dto.FoodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodQueryServiceImpl implements FoodQueryService {
    private final FoodRepository foodRepository;
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
}
