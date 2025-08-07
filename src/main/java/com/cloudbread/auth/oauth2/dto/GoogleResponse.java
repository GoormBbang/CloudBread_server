package com.cloudbread.auth.oauth2.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

// Google oauth2 응답에서 필요한 정보를 추출하는 역할
@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response {
    private final Map<String, Object> attributes;
    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
    } // 고유 식별자

    @Override
    public String getEmail() {
        return attributes.get("email").toString(); // 이메일
    }

    @Override
    public String getNickName() {
        return attributes.get("name").toString(); // 닉네임
    }

    @Override
    public String getProfileImage() {
        return attributes.get("picture").toString(); // 프로필 사진 URL
    }
}
