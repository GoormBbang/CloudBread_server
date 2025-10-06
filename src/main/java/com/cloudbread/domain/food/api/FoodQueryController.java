package com.cloudbread.domain.food.api;

import com.cloudbread.domain.food.application.FoodQueryService;
import com.cloudbread.domain.food.dto.FoodResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodQueryController {
    private final FoodQueryService foodQueryService;

    @GetMapping("/suggest")
    public BaseResponse<List<FoodResponse.SuggestItem>> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        var result = foodQueryService.suggest(q, limit);
        return BaseResponse.onSuccess(SuccessStatus._OK, result);
    }

}
