package com.cloudbread.domain.food_history.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.food_history.application.FoodHistoryService;
import com.cloudbread.domain.food_history.dto.FoodHistoryRequest;
import com.cloudbread.domain.food_history.dto.FoodHistoryResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FoodHistoryRestController {
    private final FoodHistoryService foodHistoryService;

    @PostMapping("/food-history")
    public BaseResponse<FoodHistoryResponse.Created> create(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody FoodHistoryRequest.Create req
    ) {
        Long userId = principal.getUserId();
        var res = foodHistoryService.create(userId, req);
        return BaseResponse.onSuccess(SuccessStatus.FOOD_HISTORY_CREATED, res);
    }
}
