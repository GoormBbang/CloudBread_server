package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;
import com.cloudbread.domain.feedback.dto.FeedbackResponseDto;

import com.cloudbread.global.common.response.BaseResponse;


public interface FeedbackService {

    /**
     * FastAPI 호출 -> 피드백 DB 저장 -> Response 리턴
     */

    //String generateFeedback(Long userId);
    BaseResponse<FeedbackResponseDto.Result> generateFeedback(Long userId);

    //FeedbackResponseDto generateFeedback(Long userId);

}


