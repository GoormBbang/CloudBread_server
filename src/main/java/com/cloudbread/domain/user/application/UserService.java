package com.cloudbread.domain.user.application;

import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto.Example exampleMethod(Long userId);

    UserResponseDto.UpdateDetailsResponse updateDetails(Long userId, UserRequestDto.UpdateDetailsRequest request);
}
