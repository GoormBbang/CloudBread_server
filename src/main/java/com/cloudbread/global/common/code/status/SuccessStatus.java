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

    USER_METADATA_SUCCESS(HttpStatus.OK, "MEMBER_202", "회원가입 3단계를 위한 helper API가 정상호출되었습니다."),

    USER_HEALTH_INFO_SUCCESS(HttpStatus.OK, "MEMBER_203", "회원 정보의 건강상태가 성공적으로 업데이트되었습니다."),

    USER_INFO_SUCCESS(HttpStatus.OK, "USER_200","성공적으로 내 정보를 조회했습니다." ),


    USER_INFO_UPDATE_SUCCESS(HttpStatus.OK,"USER_201","사용자 정보가 성공적으로 업데이트되었습니다." ),

    USER_DELETE_SUCCESS(HttpStatus.OK, "USER_200", "회원탈퇴가 완료되었습니다."),

    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "USER_204", "유저 프로필이 성공적으로 변경되었습니다"),

    // token
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "TOKEN_300", "토큰이 정상적으로 재발급되었습니다."),


    //mealplan
    MEAL_PLAN_CREATE_SUCCESS(HttpStatus.OK, "MEALPLAN_200", "AI 추천 식단 생성 요청 완료"),

    // photo-analysis
    PHOTO_UPLOAD_SUCCESS(HttpStatus.OK, "PHOTO_400", "사진이 정상적으로 업로드되었습니다."),

    // food-history
    FOOD_HISTORY_CREATED(HttpStatus.CREATED, "FOOD_HISTORY_CREATED", "섭취 기록이 저장되었습니다"),
    FOOD_HISTORY_TODAY_SUCCESS(HttpStatus.OK, "FOOD_HISTORY_TODAY_SUCCESS", "오늘 먹은 음식 조회 완료"),


    // chat session
    SESSION_CREATED_SUCCESS(HttpStatus.CREATED, "SESSION_CREATED", "세션이 성공적으로 생성되었습니다"),

    //nutrition
    NUTRITION_STATS_SUCCESS(HttpStatus.OK, "NUTRITION_STATS_SUCCESS", "영양 분석이 성공적으로 조회되었습니다."),
    NUTRITION_SUMMARY_SUCCESS(HttpStatus.OK, "NUTRITION_SUMMARY_SUCCESS", "영양 요약 성공."),
    //NUTRITION_SUMMARY_SUCCESS(HttpStatus.OK, "NUTRITION_SUMMARY_SUCCESS", "영양 "),

    //calender
    CALENDAR_GET_SUCCESS(HttpStatus.OK, "CALENDAR_MONTH_SUCCESS", "조회 가능한 식단이 있습니다."),
    CALENDAR_SUMMERY_SUCCESS(HttpStatus.OK, "CALENDAR_SUMMERY_SUCCESS", "상세 조회 성공."),

    //nutrient
    NUTRITION_BALANCE_SUCCESS(HttpStatus.OK, "NUTRITION_BALANCE_SUCCESS", "영양 밸런스 조회 성공"),

    // notification
    NOTIFICATION_LIST_SUCCESS(HttpStatus.OK, "NOTIFICATION_LIST_SUCCESS", "알림함 리스트 조회 성공"),
    NOTIFICATION_DELETE_SUCCESS(HttpStatus.OK, "NOTIFICATION_DELETE_SUCCESS", "알림함 삭제 성공"),

    //feedback
    FEEDBACK_SUCCESS(HttpStatus.OK, "FEEDBACK_200", "FastAPI 피드백 요청 완료"),

    //ALERT_SETTING
    ALERT_SETTING_UPDATE_SUCCESS(HttpStatus.OK, "ALERT_SETTING_UPDATE_SUCCESS", "알림 설정이 성공적으로 수정되었습니다."),
    ALERT_SETTING_SUCCESS(HttpStatus.OK, "ALERT_SETTING_SUCCESS", "알림 설정 조회 성공")

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
