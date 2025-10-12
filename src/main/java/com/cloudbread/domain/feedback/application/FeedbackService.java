package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;

public interface FeedbackService {

    /**
     * FastAPI 호출 -> 피드백 DB 저장 -> Response 리턴
     */
    String generateFeedback(Long userId);
}


