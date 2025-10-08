package com.cloudbread.domain.notifiaction.application.filter;

import com.cloudbread.auth.jwt.JwtUtil;
import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.user.domain.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 브라우저 EventSource는 Authorization 헤더를 못 받기에, JwtAuthorizationFilter 전에,
 * 구독 엔드포인트만, 쿼리파라미터로부터 token을 읽어, SecurityContext에 principal을 넣어주는 전용 필터
 */

@RequiredArgsConstructor
@Slf4j
public class SseQueryParamAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 이 필터는 SSE 구독 엔드포인트에만 동작
        String uri = request.getRequestURI();
        return !matcher.match("/api/notifications/subscribe", uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Authorization 헤더가 없고, token 파라미터가 있으면 거기서 인증 시도
        String auth = request.getHeader("Authorization");
        String tokenParam = request.getParameter("token");

        if ((auth == null || !auth.startsWith("Bearer ")) && tokenParam != null && !tokenParam.isBlank()) {
            String accessToken = tokenParam.trim();

            try {
                if (jwtUtil.isExpired(accessToken)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().print("access token expired");
                    return;
                }
                if (!"accessToken".equals(jwtUtil.getTokenCategory(accessToken))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().print("invalid access token");
                    return;
                }

                Long userId = jwtUtil.getUserId(accessToken);
                String email = jwtUtil.getEmail(accessToken);

                User user = User.createUserForSecurityContext(userId, email);
                CustomOAuth2User principal = new CustomOAuth2User(user);

                var authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                log.warn("[SSE-AUTH] token param auth failed", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}