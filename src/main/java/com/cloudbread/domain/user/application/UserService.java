package com.cloudbread.domain.user.application;

import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto.Example exampleMethod(Long userId);
    UserResponseDto.UpdateResponse updateDetails(Long userId, UserRequestDto.UpdateDetailsRequest request);
    UserResponseDto.MetadataResponse getMetaData();
    UserResponseDto.UpdateResponse updateHealthInfo(Long userId, UserRequestDto.UpdateHealthInfoRequest request);
    UserResponseDto.MyInfoResponse getInfo2(Long userId);

    UserResponseDto.UpdateResponse updateMyInfo(Long userId, UserRequestDto.UpdateMyInfoRequest request);
    UserResponseDto.UserSummaryResponse getUserSummary(Long userId);
    UserResponseDto.UpdateUserSummaryResponse updateUserSummary(Long userId, UserRequestDto.UpdateUserSummaryRequest request);


    void logout(Long userId);

}
