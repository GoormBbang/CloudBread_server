package com.cloudbread.domain.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class FeedbackResponseDto {
    @JsonProperty("isSuccess")
    private boolean isSuccess; // ✅ FastAPI JSON과 일치
    private String code;
    private String message;
    @JsonProperty("result")
    private Result result;

    @Getter
    @Setter
    public static class Result {
        @JsonProperty("FeedbackDate")
        private String feedbackDate;
        @JsonProperty("FeedbackSummary")
        private String feedbackSummary;
    }
}

