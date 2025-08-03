package com.cloudbread.domain.user.exception.annotation;

/*
    어노테이션 선언부
 */

import com.cloudbread.domain.user.exception.validator.UserExistValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UserExistValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserExist {
    String message() default "해당하는 User가 ID가 존재하지 않습니다."; // 검증 실패 시, 메세지

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
