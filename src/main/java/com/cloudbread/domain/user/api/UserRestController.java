package com.cloudbread.domain.user.api;

import com.cloudbread.domain.user.application.UserService;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.domain.user.dto.UserResponseDto.Example;
import com.cloudbread.domain.user.exception.annotation.UserExist;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    아래 /api/users/example//{user-id} 는 샘플 api 이다
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {
    private final UserService userService;

    @GetMapping("/example/{user-id}")
    public BaseResponse<UserResponseDto.Example> getUser(
        @PathVariable(name = "user-id") @Valid @UserExist Long userId
    ){
        UserResponseDto.Example result = userService.exampleMethod(userId);

        return BaseResponse.onSuccess(SuccessStatus.USER_EXAMPLE_SUCCESS, result);
    }


}


