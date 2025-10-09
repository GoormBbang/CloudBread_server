package com.cloudbread.domain.user.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class FileInvalidException extends GeneralException {
    public FileInvalidException(){
        super(ErrorStatus.FILE_INVALID);
    }
}
