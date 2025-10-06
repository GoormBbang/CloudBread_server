package com.cloudbread.domain.food.dto;

public class FoodResponse {
    public record SuggestItem(Long foodId, String name, String category, java.math.BigDecimal calories) {}
}
