package com.cloudbread.domain.user.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.user.application.UserService;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
//@Tag(name = "Profile Controller", description = "사용자 프로필 조회 API")
public class ProfileController {

    private final UserService userService;

    //@Operation(summary = "프로필 조회", description = "로그인한 사용자의 이름과 생년월일을 조회합니다.")
    @GetMapping("/users/profile")
    public BaseResponse<UserResponseDto.ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        UserResponseDto.ProfileResponse result = userService.getUserProfile(userId);
        return BaseResponse.onSuccess(SuccessStatus.USER_INFO_SUCCESS, result);
    }
}
