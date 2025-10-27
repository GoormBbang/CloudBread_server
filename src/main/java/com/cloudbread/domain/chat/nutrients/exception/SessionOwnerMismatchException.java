package com.cloudbread.domain.chat.nutrients.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class SessionOwnerMismatchException extends GeneralException {
    public SessionOwnerMismatchException(){
        super(ErrorStatus.SESSION_MISMATCH);
    }
}
