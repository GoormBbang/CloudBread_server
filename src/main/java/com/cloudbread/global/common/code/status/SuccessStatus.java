package com.cloudbread.global.common.code.status;

import com.cloudbread.global.common.code.BaseCode;
import com.cloudbread.global.common.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),
    _CREATED(HttpStatus.CREATED, "COMMON201", "요청 성공 및 리소스 생성됨"),

    // user
    USER_EXAMPLE_SUCCESS(HttpStatus.OK,"MEMBER_200","성공적으로 조회되었습니다."),

    USER_REGISTER_DETAIL(HttpStatus.OK, "MEMBER_201", "회원정보가 성공적으로 업데이트되었습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
