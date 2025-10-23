package com.cloudbread.domain.test_user;

import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.token.domain.Token;
import com.cloudbread.auth.token.domain.TokenRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.enums.OauthProvider;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevMockAuthController {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    public record MintTokenRequest(String email,
                                   String nickname,
                                   String profileImageUrl,
                                   String provider  // "KAKAO","GOOGLE","NAVER"
    ) {}

    @PostMapping("/mint-token")
    public Map<String, Object> mintToken(@RequestBody MintTokenRequest req) {
        String email = req.email() != null ? req.email() : "test@naver.com";
        String nickname = req.nickname() != null ? req.nickname() : "테스터";
        String profile = req.profileImageUrl() != null ? req.profileImageUrl()
                : "http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg";
        OauthProvider provider = req.provider() != null ? OauthProvider.valueOf(req.provider()) : OauthProvider.KAKAO;

        // 1) 없으면 1차 회원가입만 수행
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.createUserFirstOAuth(email, nickname, profile, provider);
            return userRepository.save(u);
        });

        Long userId = user.getId();

        // 2) JWT 발급 (너의 JwtUtil 서명/클레임 형식 그대로 사용)
        String accessToken = jwtUtil.createAccessToken("accessToken", userId, email, 24L * 60 * 60 * 1000);
        String refreshToken = jwtUtil.createRefreshToken("refreshToken", userId, 30L * 24 * 60 * 60 * 1000);

        // 3) refreshToken은 DB에 upsert
        tokenRepository.findByUserId(userId).ifPresentOrElse(token -> {
            token.update(refreshToken, LocalDateTime.now().plusDays(30));
            tokenRepository.save(token);
        }, () -> {
            tokenRepository.save(Token.toEntity(user, refreshToken, LocalDateTime.now().plusDays(30)));
        });

        // 4) 응답
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "nickname", user.getNickname(),
                        "profileImageUrl", user.getProfileImageUrl(),
                        "provider", user.getOauthProvider().name()
                )
        );
    }
}