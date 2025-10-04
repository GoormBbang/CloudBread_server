package com.cloudbread.domain.tip.api;

import com.cloudbread.domain.tip.application.UserTipService;
import com.cloudbread.domain.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/tip")
@RequiredArgsConstructor
public class UserTipController {
    private final UserTipService userTipService;

    @GetMapping
    public ResponseEntity<UserResponseDto.TipResponse> getMyTips() {
        return ResponseEntity.ok(userTipService.getMyTips());
    }

    @GetMapping("/baby")
    public ResponseEntity<UserResponseDto.TipResponse> getBabyTips() {
        return ResponseEntity.ok(userTipService.getBabyTips());
    }

    @GetMapping("/mom")
    public ResponseEntity<UserResponseDto.TipResponse> getMomTips() {
        return ResponseEntity.ok(userTipService.getMomTips());
    }

    @GetMapping("/nutrition")
    public ResponseEntity<UserResponseDto.TipResponse> getNutritionTips() {
        return ResponseEntity.ok(userTipService.getNutritionTips());
    }
}


