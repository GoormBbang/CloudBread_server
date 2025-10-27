package com.cloudbread.domain.food.api;

import com.cloudbread.domain.food.application.FoodQueryService;
import com.cloudbread.domain.food.dto.FoodResponse;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 검색/자동완성 폴백: foodId로 상세 조회
     * 응답 result는 confirm에서의 selected와 동일한 필드 구성(ConfirmSelected)
     */
    @GetMapping("{foodId}/detail")
    public BaseResponse<PhotoAnalysisResponse.ConfirmSelected> getFoodDetail(
            @PathVariable Long foodId
    ) {
        var selected = foodQueryService.getFoodDetail(foodId);
        return BaseResponse.onSuccess(SuccessStatus._OK, selected);
    }

}
