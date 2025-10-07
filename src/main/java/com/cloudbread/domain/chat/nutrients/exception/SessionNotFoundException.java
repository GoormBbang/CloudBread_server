package com.cloudbread.domain.chat.nutrients.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class SessionNotFoundException extends GeneralException{
    public SessionNotFoundException(){
        super(ErrorStatus.SESSION_NOT_FOUND);
    }
}


