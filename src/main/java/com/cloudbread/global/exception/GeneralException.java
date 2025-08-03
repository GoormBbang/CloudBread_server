package com.cloudbread.global.exception;

import com.cloudbread.global.common.code.BaseErrorCode;

public abstract class GeneralException extends RuntimeException{
    private final BaseErrorCode errorCode;

    protected GeneralException(BaseErrorCode errorCode){
        super(errorCode.getReasonHttpStatus().getMessage());
        this.errorCode = errorCode;
    }

    public BaseErrorCode getErrorCode(){
        return errorCode;
    }
}
