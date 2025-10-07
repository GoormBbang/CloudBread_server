package com.cloudbread.domain.chat.main.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class MissingTopicException extends GeneralException {
    public MissingTopicException(){
        super(ErrorStatus.TOPIC_REQUIRED);
    }
}
