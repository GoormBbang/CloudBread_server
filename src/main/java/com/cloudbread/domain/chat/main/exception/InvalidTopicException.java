package com.cloudbread.domain.chat.main.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class InvalidTopicException extends GeneralException {
    public InvalidTopicException(){
        super(ErrorStatus.TOPIC_INVALID);
    }
}
