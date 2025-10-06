package com.cloudbread.domain.food.application;

import com.cloudbread.domain.food.dto.FoodResponse;

import java.util.List;

public interface FoodQueryService {
    List<FoodResponse.SuggestItem> suggest(String q, int limit);
}
