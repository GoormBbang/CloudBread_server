package com.cloudbread.domain.chat.nutrients.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class SessionExpiredException extends GeneralException {
    public SessionExpiredException(){
        super(ErrorStatus.SESSION_EXPIRED);
    }
}
