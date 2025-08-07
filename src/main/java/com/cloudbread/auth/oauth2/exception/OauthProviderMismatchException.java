package com.cloudbread.auth.oauth2.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class OauthProviderMismatchException extends OAuth2AuthenticationException {
    public OauthProviderMismatchException(String message) {
        super(new OAuth2Error("provider_mismatch", message, null));
    }
}
