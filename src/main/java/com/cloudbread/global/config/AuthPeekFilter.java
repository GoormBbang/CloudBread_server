package com.cloudbread.global.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
@Order(5) // JwtAuthorizationFilter보다 뒤로 (숫자는 환경에 맞게)
public class AuthPeekFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        if (r.getRequestURI().startsWith("/api/")) {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            log.info("[PEEK:before] {} {} | auth={} principal={}",
                    r.getMethod(), r.getRequestURI(),
                    a==null?null:a.getClass().getSimpleName(),
                    a==null?null:a.getPrincipal().getClass().getName());
        }
        try {
            chain.doFilter(req, res);
        } finally {
            HttpServletRequest r2 = (HttpServletRequest) req;
            if (r2.getRequestURI().startsWith("/api/")) {
                Authentication a2 = SecurityContextHolder.getContext().getAuthentication();
                log.info("[PEEK:after ] {} {} | auth={} principal={}",
                        r2.getMethod(), r2.getRequestURI(),
                        a2==null?null:a2.getClass().getSimpleName(),
                        a2==null?null:a2.getPrincipal().getClass().getName());
            }
        }
    }
}