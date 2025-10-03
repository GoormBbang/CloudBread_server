package com.cloudbread.auth.jwt;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.user.domain.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JWT 검증 필터
// 1. 헤더에서 accessToken 추출, 2. 토큰 검증, 3. 유효하면 사용자정보를 SecurityContextHolder에 세팅
// 그러면, 이후 컨트롤러에서 @AuthenticationPrincipal에서 저장했던 사용자 정보를 꺼내쓸 수 있음
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 인증헤더 Bearer가 없다면, 다음 필터로 넘김
        if (header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return ;
        }

        String accessToken = header.substring(7);


        // 토큰 만료 여부 확인, 만료 시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e){

            // response status code + msg
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("access token expired");

            return;
        }

        // 토큰이 accessToken 종류인지 확인
        String tokenCategory = jwtUtil.getTokenCategory(accessToken);

        if (!tokenCategory.equals("accessToken")){
            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("invalid access token");

            return;
        }

        // userId와 email 값 추출
        Long userId = jwtUtil.getUserId(accessToken);
        String email = jwtUtil.getEmail(accessToken);

        User user = User.createUserForSecurityContext(userId, email);

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user);

        // 스프링 시큐리티 인증 토큰 생성
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities());

        // 생성한 인증 정보를 SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


        filterChain.doFilter(request, response);

    }
}
