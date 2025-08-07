package com.cloudbread.auth.oauth2.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

// kakao oauth2 응답에서 필요한 정보를 추출하는 역할
@RequiredArgsConstructor
public class KaKaoResponse implements OAuth2Response {
    private final Map<String, Object> attributes;
    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getNickName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("nickname").toString();
    }

    @Override
    public String getProfileImage() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("profile_image").toString();
    }
}
