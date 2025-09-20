package com.cloudbread.auth.token.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class TokenUnauthorizedException extends GeneralException {

    public TokenUnauthorizedException() {
        super(ErrorStatus.TOKEN_UNAUTHORIZED);
    }

}
