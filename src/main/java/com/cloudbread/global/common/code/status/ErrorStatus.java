package com.cloudbread.global.common.code.status;

import com.cloudbread.global.common.code.BaseErrorCode;
import com.cloudbread.global.common.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // --- Common ---
    _INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON400", "입력값이 올바르지 않습니다"),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "권한이 없습니다."),
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // --- User ---
    NO_SUCH_USER(HttpStatus.BAD_REQUEST, "USER_404", "User가 존재하지 않습니다."),
    USER_ALREADY_DEACTIVATED(HttpStatus.CONFLICT, "USER_409", "이미 탈퇴한 사용자입니다."),
    FILE_INVALID(HttpStatus.BAD_REQUEST, "FILE_400", "파일이 비어있거나, 이미지 파일을 업로드하지 않은 경우입니다. 점검해주세요."),

    // --- Token ---
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN_404", "리프레시 토큰을 찾을 수 없습니다."),
    TOKEN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "TOKEN_401", "리프레시 토큰 인증 실패입니다."),


    // --- Session ---
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_404", "세션이 존재하지 않습니다."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "SESSION_401", "세션이 만료되었습니다. 다시 발급해주세요"),
    SESSION_MISMATCH(HttpStatus.CONFLICT, "SESSION_409", "세션유저와 로그인한유저의 정보가 일치하지 않습니다"),
    TOPIC_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_400", "topic 필드는 필수값입니다"),
    TOPIC_INVALID(HttpStatus.BAD_REQUEST, "TOPIC_400", "topic 필드는 FOOD_INFO, PREGNANCY_DRUG, PREGNANCY, FREE 중 하나여야 합니다."),


    //calender
    CALENDAR_GET_EMPTY(HttpStatus.NO_CONTENT, "CALENDAR_MONTH_EMPTY", "조회 가능한 식단이 없습니다."),
    CALENDAR_SUMMARY_FAIL(HttpStatus.NO_CONTENT, "CALENDAR_SUMMARY_FAIL", "상세 조회 실패."),

    //food
    FOOD_HISTORY_TODAY_FAIL(HttpStatus.NO_CONTENT, "FOOD_HISTORY_TODAY_FAIL", "오늘 먹은 음식 조회 실패."),
    // --- Nutrition ---
    NUTRITION_SUMMARY_FAIL(HttpStatus.NO_CONTENT, "NUTRITION_SUMMARY_FAIL", "영양 요약 실패."),
    NUTRITION_BALANCE_FAIL(HttpStatus.NO_CONTENT, "NUTRITION_BALANCE_FAIL", "영양 밸런스 조회 실패"),

    //
    FEEDBACK_FAIL(HttpStatus.NO_CONTENT, "FEEDBACK_FAIL", "피드백 실패"),
    //ALERT_SETTING
    ALERT_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "ALERT_SETTING_NOT_FOUND", "알림 설정 조회 실패")


    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }



}
