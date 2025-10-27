package com.cloudbread.domain.user.exception;

import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.exception.GeneralException;

public class UserAlreadyDeactivatedException extends GeneralException {

  public UserAlreadyDeactivatedException() {
    super(ErrorStatus.USER_ALREADY_DEACTIVATED);
  }

}
