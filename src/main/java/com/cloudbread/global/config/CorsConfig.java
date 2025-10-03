package com.cloudbread.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// React, Vite 연동 허용 -> 개발편의성을 위해 모든 cors 요청 허용
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*")); // 모든 출처 허용
        // 자격 증명(쿠키, Authorization 헤더 등) 허용
        config.setAllowCredentials(true);

        // 모든 HTTP 메서드 허용
        config.setAllowedMethods(List.of("*"));

        // 모든 요청 헤더 허용 (Authorization 헤더 포함)
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); //** 뜻은 모든 URL 경로에 적용한다는 의미
        return source;
    }
}