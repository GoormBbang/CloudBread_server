package com.cloudbread.auth.token.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class RefreshTokenNotFoundException extends GeneralException {

    public RefreshTokenNotFoundException() {
        super(ErrorStatus.REFRESH_TOKEN_NOT_FOUND);
    }

}
