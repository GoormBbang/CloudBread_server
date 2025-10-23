package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;
import com.cloudbread.domain.feedback.dto.FeedbackResponseDto;

public interface FeedbackService {

    /**
     * FastAPI 호출 -> 피드백 DB 저장 -> Response 리턴
     */
    FeedbackResponseDto generateFeedback(Long userId);
}


