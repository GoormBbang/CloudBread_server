package com.cloudbread.global.exception;

import com.cloudbread.global.common.code.ErrorReasonDTO;
import com.cloudbread.global.common.code.status.ErrorStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {
    /**
     * 커스텀 GeneralException 처리
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<BaseResponse<?>> handleGeneralException(GeneralException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        ErrorReasonDTO errorReason = ex.getErrorCode().getReasonHttpStatus();
        log.warn("[handleGeneralException] {}: {} | path : {}", errorReason.getCode(), errorReason.getMessage(), path);

        BaseResponse<?> errorResponse = BaseResponse.onFailure(ex.getErrorCode(), path);
        return new ResponseEntity<>(errorResponse, errorReason.getHttpStatus());
    }

    /**
     * @Valid 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                 HttpServletRequest request) {
        String path = request.getRequestURI();
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("[handleMethodArgumentNotValidException] {} | path : {}", errorMessage, path);

        BaseResponse<?> errorResponse = BaseResponse.onFailure(
                ErrorStatus._INVALID_INPUT_VALUE,
                errorMessage,
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 커스텀 annotation 에러 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                              HttpServletRequest request){
        String path = request.getRequestURI();
        String errorMessage = ex.getConstraintViolations().stream()
                .map(constraintViolation -> constraintViolation.getMessage())
                .findFirst()
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "ConstraintViolationException 추출 도중 에러 발생")
                );
        log.warn("[handleConstraintViolationException] {} | path : {}", errorMessage, path);

        BaseResponse<Object> errorResponse = BaseResponse.onFailure(
                ErrorStatus._BAD_REQUEST,
                errorMessage,
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public void onAuthEx(AuthenticationException e) {
        log.error("[AUTH-EX] {}", e.toString(), e);
        throw e; // 기존 흐름 유지 (EntryPoint가 처리)
    }
    @ExceptionHandler(AccessDeniedException.class)
    public void onDenied(AccessDeniedException e) {
        log.error("[DENIED-EX] {}", e.toString(), e);
        throw e;
    }


    /**
     * 그 외 모든 예상 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.error("[handleUnexpectedException] 알 수 없는 예외 발생 | path : {}", path, ex);

        BaseResponse<?> errorResponse = BaseResponse.onFailure(
                ErrorStatus._INTERNAL_SERVER_ERROR,
                path
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
