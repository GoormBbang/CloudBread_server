package com.cloudbread.auth.oauth2.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;


@RequiredArgsConstructor
public class NaverResponse implements OAuth2Response {
    private final Map<String, Object> attributes; // 이건 { resultcode, message, response: {...} }

    private Map<String, Object> responseMap() {
        return (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return responseMap().get("id").toString();
    }

    @Override
    public String getEmail() {
        return responseMap().get("email").toString(); // ✅ response 내부 접근
    }

    @Override
    public String getNickName() {
        return responseMap().get("nickname").toString();
    }

    @Override
    public String getProfileImage() {
        return responseMap().get("profile_image").toString();
    }
}
