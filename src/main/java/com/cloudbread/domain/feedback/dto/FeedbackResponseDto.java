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
    private boolean isSuccess;
    private String code;
    private String message;

    @JsonProperty("result")
    private Result result;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Result {
        @JsonProperty("FeedbackDate")
        private String feedbackDate;
        @JsonProperty("FeedbackSummary")
        private String feedbackSummary;
    }
}

