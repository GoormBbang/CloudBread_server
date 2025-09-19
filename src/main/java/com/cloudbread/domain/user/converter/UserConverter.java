package com.cloudbread.domain.user.converter;

import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.dto.UserResponseDto;

public class UserConverter {

    public static UserResponseDto.Example toExample(User user){
        return UserResponseDto.Example
                .builder()
                .id(user.getId())
                .nickname(user.getNickname())
         //       .age(user.getAge())
         //       .isAdult(user.isAdult())
                .build();
    }

}
