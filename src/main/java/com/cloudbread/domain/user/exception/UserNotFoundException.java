package com.cloudbread.domain.user.exception;

import com.cloudbread.global.common.code.BaseErrorCode;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class UserNotFoundException extends GeneralException {

    public UserNotFoundException() {
        super(ErrorStatus.NO_SUCH_USER);
    }

}
