package com.cloudbread.domain.user.exception;

import com.cloudbread.global.common.code.BaseErrorCode;
import com.cloudbread.global.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(BaseErrorCode errorCode) {
        super(errorCode);
    }

}
