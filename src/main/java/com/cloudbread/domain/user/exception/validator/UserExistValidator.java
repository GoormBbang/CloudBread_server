package com.cloudbread.domain.user.exception.validator;

import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.annotation.UserExist;
import com.cloudbread.global.common.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
    UserExist 어노테이션, 실제 검증 로직
 */
@Component
@RequiredArgsConstructor
public class UserExistValidator implements ConstraintValidator<UserExist, Long> {
    private final UserRepository userRepository;

    @Override
    public void initialize(UserExist constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        boolean isValid = userRepository.existsById(userId);

        if (!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_USER.toString()).addConstraintViolation();
        }

        return isValid;
    }

}
