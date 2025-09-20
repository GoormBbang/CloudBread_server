package com.cloudbread.auth.oauth2;

import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.token.domain.Token;
import com.cloudbread.auth.token.domain.TokenRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

// 카카오 로그인 성공 시, 콜백 핸들러
// 1. JWT 토큰 발급
// - 이때, JWT payload는 보안상 최소한의 정보(userId, email)만 담겠다
// 2. refreshToken만 DB에 저장
// 3. JSON 응답으로, accessToken과 refreshToken 을 반환해준다.

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException  {
        log.info("OAuth2LoginSuccessHandler는 왔음 ??");
        log.info("onAuthenticationSuccess 요청 URI: {}", request.getRequestURI());

        // 1. CustomOAuth2UserService에서 설정한 OAuth2User 정보 가져오기
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        User user = customUserDetails.getUser();
        Long userId = customUserDetails.getUserId();
        String email = customUserDetails.getEmail();

        log.info(" >>>> OAuth2LoginSuccessHandler :: user, userId, email:: {} {} {}", user, userId, email);

        // 2. 1)의 사용자 정보를 담아, accessToken과 refreshToken 발행
        String accessToken = jwtUtil.createAccessToken("accessToken", userId, email, 30 * 60 * 1000L);// 유효기간 30분
        String refreshToken = jwtUtil.createRefreshToken("refreshToken", userId, 30 * 24 * 60 * 60 * 1000L);    // 유효기간 30일

        // 3. refreshToken을 DB에 저장 -- user1명당, token 1개로 제한해놓아서 업데이트 로직으로 변경
        Optional<Token> existingToken = tokenRepository.findByUserId(userId);

        if (existingToken.isPresent()){
            log.info("이미 토큰이 존재합니다. 새로운 토큰으로 업데이트합니다!");
            Token token = existingToken.get();
            token.update(refreshToken, LocalDateTime.now().plusDays(30));

            tokenRepository.save(token);

        } else { // 기존 토큰 존재하지 않음
            Token refreshTokenEntity = Token.toEntity(user, refreshToken, LocalDateTime.now().plusDays(30));
            tokenRepository.save(refreshTokenEntity);
        }

        // 4. JSON 응답 생성
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, Object> body = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", Map.of(
                        "id", user.getId(),
                        "nickname", user.getNickname(),
                        "profileImageUrl", user.getProfileImageUrl()
                )
        );

        new ObjectMapper().writeValue(response.getWriter(), body);


    }
}
