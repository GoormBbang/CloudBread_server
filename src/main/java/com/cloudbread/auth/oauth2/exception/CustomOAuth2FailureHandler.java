package com.cloudbread.auth.oauth2.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "OAuth Login Failed");

        if (exception instanceof OAuth2AuthenticationException ex) {
            errorResponse.put("message", ex.getError().getDescription());
        } else {
            errorResponse.put("message", exception.getMessage());
        }

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
