package com.cloudbread.global.config;

import com.cloudbread.auth.jwt.JwtAuthorizationFilter;
import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.oauth2.CustomOAuth2UserService;
import com.cloudbread.auth.oauth2.OAuth2LoginSuccessHandler;
import com.cloudbread.auth.oauth2.exception.CustomOAuth2FailureHandler;
import com.cloudbread.domain.notifiaction.application.filter.SseQueryParamAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity // security 활성화 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtUtil jwtUtil;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    private final CorsConfigurationSource corsConfigurationSource;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(cs -> cs.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) //  CORS 설정 추가
                .formLogin(AbstractHttpConfigurer::disable) // 시큐리티 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                // oauth2 로그인
                //  - userInfoEndPoint에서 사용자 정보 불러오고
                //  - successHandler에서 로그인 성공 시 JWT 생성 및 반환로직
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(a -> a.baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(r -> r.baseUri("/api/login/oauth2/code/*"))
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(customOAuth2FailureHandler) // 실패 시 핸들러 등록

                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 X (jwt토큰)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/swagger-ui/**", "/api/v3/api-docs/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/api/oauth2/authorization/**",
                                "/api/login/oauth2/code/**",
                                "/api/users/example/{user-id}", // 패키지 구조 예제 코드
                                "/oauth2/authorization/**", // 카카오 로그인 요청 (/kakao, /google
                                "/login/oauth2/code/**", // 카카오 인증 콜백
                                "/api/metadata",
                                "/api/admin/foods/import", // foods 데이터 seeder,
                                "/api/refresh-token", // refresh token (토큰 갱신),
                                "/crawl",// crawling
                                "/seed/**",
                                "/api/ai/**", // ai가 백엔드로 요청보낼 때, 토큰 요청하지 않도록,
                                "/uploads/**",
                                "/api/photo-analyses/*/events", // 프론트가 이벤트 구독하는 api,
                                "/api/foods/suggest", // 음식 검색 api
                                "/api/foods/*/detail",
                                "/api/dev/notify"
                        )
                        .permitAll()
                        .anyRequest().authenticated() // 그외 요청은 허가된 사람만 인가

                )
                // jwtFilter : SseQueryParamAuthFilter → JwtAuthorizationFilter → UsernamePasswordAuthenticationFilter
                .addFilterBefore(new SseQueryParamAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

}
