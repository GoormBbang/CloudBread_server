package com.cloudbread.global.common.response;

import com.cloudbread.global.common.code.BaseCode;
import com.cloudbread.global.common.code.BaseErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "path", "result"}) // JSON 반환 시, 필드 순서 지정
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path; // 요청 URI (예외용)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 성공 응답
    public static <T> BaseResponse<T> onSuccess(BaseCode code, T result) {
        return new BaseResponse<>(
                true,
                code.getReasonHttpStatus().getCode(),
                code.getReasonHttpStatus().getMessage(),
                null,
                result);
    }

    // 실패 응답 (에러 + 추가 result)
    public static <T> BaseResponse<T> onFailure(BaseErrorCode code, T result) {
        return new BaseResponse<>(
                false,
                code.getReasonHttpStatus().getCode(),
                code.getReasonHttpStatus().getMessage(),
                null,
                result);
    }


    // 예외 응답 (에러 + path)
    public static <T> BaseResponse<T> onFailure(BaseErrorCode code, String path) {
        return new BaseResponse<>
                (false,
                        code.getReasonHttpStatus().getCode(),
                        code.getReasonHttpStatus().getMessage(),
                        path,
                        null);
    }

    // 예외 응답 (에러 + errorMessage + path)
    public static <T> BaseResponse<T> onFailure(BaseErrorCode code, String errorMessage, String path) {
        return new BaseResponse<>
                (false,
                        code.getReasonHttpStatus().getCode(),
                        errorMessage,
                        path,
                        null);
    }
}
