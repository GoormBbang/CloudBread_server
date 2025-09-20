package com.cloudbread.auth.token.application;

import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.token.domain.Token;
import com.cloudbread.auth.token.domain.TokenRepository;
import com.cloudbread.auth.token.dto.TokenResponseDto;
import com.cloudbread.auth.token.exception.RefreshTokenNotFoundException;
import com.cloudbread.auth.token.exception.TokenUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenService {
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    public TokenResponseDto.AuthenticationResponse reissueToken(String refreshToken) {
        Long userId = jwtUtil.getUserId(refreshToken);
        String email = jwtUtil.getEmail(refreshToken);

        // 1. refreshToken 검증
        Token token = tokenRepository.findByUserId(userId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!token.getRefreshToken().equals(refreshToken) || jwtUtil.isExpired(refreshToken)) {
            throw new TokenUnauthorizedException();
        }

        // 2. 새 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken("accessToken", userId, email, 30 * 60 * 1000L);
        String newRefreshToken = jwtUtil.createRefreshToken("refreshToken", userId, 30 * 24 * 60 * 60 * 1000L);

        // 3. DB 갱신
        tokenRepository.delete(token);
        tokenRepository.flush(); // 즉시 delete 쿼리 날림 -- 안 그러면 유저당 토큰 1개인 것이 유지되지 않아서, 예외가 발생했음..
        log.info("TokenServiceImpl -- token delete 완료");
        tokenRepository.save(Token.toEntity(token.getUser(), newRefreshToken, LocalDateTime.now().plusDays(30)));
        log.info("TokenServiceImpl -- token save 완료");

        return TokenResponseDto.AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}

