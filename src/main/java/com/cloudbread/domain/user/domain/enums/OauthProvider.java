package com.cloudbread.domain.user.domain.enums;

// 가장 최근 로그인할 때 사용된 oauth2 provider
public enum OauthProvider {
    GOOGLE, NAVER, KAKAO;

    public static OauthProvider fromRegistrationId(String registrationId){
        System.out.println(">>> registrationId 받음: " + registrationId);  // 로그 추가

        if (registrationId == null || registrationId.isBlank()) {
            throw new IllegalArgumentException("registrationId is null or blank");
        }

        switch (registrationId.toLowerCase()) {
            case "kakao": return KAKAO;
            case "google": return GOOGLE;
            case "naver": return NAVER;
            default: throw new IllegalArgumentException("Unknown registrationId: " + registrationId);
        }
    }

}
