package com.cloudbread.global.config;

import com.cloudbread.auth.jwt.JwtAuthorizationFilter;
import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.oauth2.CustomOAuth2UserService;
import com.cloudbread.auth.oauth2.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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

    private final CorsConfigurationSource corsConfigurationSource;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable) // 아직 백엔드만 이기에 cors 비활성화
                .csrf(AbstractHttpConfigurer::disable) // csrf 방어 기능 비활성화 (jwt 토큰을 사용할 것이기에)
                .formLogin(AbstractHttpConfigurer::disable) // 시큐리티 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                // oauth2 로그인
                //  - userInfoEndPoint에서 사용자 정보 불러오고
                //  - successHandler에서 로그인 성공 시 JWT 생성 및 반환로직
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)

                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 X (jwt토큰)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/api/users/example/{user-id}", // 패키지 구조 예제 코드
                                "/oauth2/authorization/kakao", // 카카오 로그인 요청
                                "/login/oauth2/code/**" // 카카오 인증 콜백
                        )
                        .permitAll()
                        .anyRequest().authenticated() // 그외 요청은 허가된 사람만 인가

                )
                // jwtFilter
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

}
