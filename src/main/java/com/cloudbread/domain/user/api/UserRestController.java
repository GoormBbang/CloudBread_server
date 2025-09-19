package com.cloudbread.domain.user.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.user.application.UserService;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.domain.user.dto.UserResponseDto.Example;
import com.cloudbread.domain.user.exception.annotation.UserExist;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/*
    아래 /api/users/example//{user-id} 는 샘플 api 이다
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {
    private final UserService userService;

    // 회원가입 STEP2 api
    @PutMapping("/details")
    public BaseResponse<UserResponseDto.UpdateResponse> updateDetails(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestBody @Valid UserRequestDto.UpdateDetailsRequest request
    ){
        Long userId = customOAuth2User.getUserId();
        UserResponseDto.UpdateResponse result = userService.updateDetails(userId, request);

        return BaseResponse.onSuccess(SuccessStatus.USER_REGISTER_DETAIL, result);

    }

    @GetMapping("/example/{user-id}")
    public BaseResponse<UserResponseDto.Example> getUser(
        @PathVariable(name = "user-id") @Valid @UserExist Long userId
    ){
        UserResponseDto.Example result = userService.exampleMethod(userId);

        return BaseResponse.onSuccess(SuccessStatus.USER_EXAMPLE_SUCCESS, result);
    }

    // SpringContextHolder에 담긴 인증정보가 잘 보이는지 체크
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto.SecurityContextDto> getMyInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
            ){
        User user = customOAuth2User.getUser();

        UserResponseDto.SecurityContextDto result = UserResponseDto.SecurityContextDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(result);
    }


}


