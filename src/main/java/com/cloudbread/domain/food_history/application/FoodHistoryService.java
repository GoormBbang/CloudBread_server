package com.cloudbread.domain.food_history.application;

import com.cloudbread.domain.food_history.dto.FoodHistoryRequest;
import com.cloudbread.domain.food_history.dto.FoodHistoryResponse;

public interface FoodHistoryService {
    FoodHistoryResponse.Created create(Long userId, FoodHistoryRequest.Create req);
}
