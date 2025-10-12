package com.cloudbread.domain.feedback.client;

import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiFeedbackClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.fastapi.feedback}")
    private String feedbackUrl;

    public ResponseEntity<String> requestRawFeedback(FeedbackRequestDto request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FeedbackRequestDto> entity = new HttpEntity<>(request, headers);
            log.info("📤 [FastAPI 요청 전송] {}", feedbackUrl);

            return restTemplate.exchange(feedbackUrl, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            log.error("❌ [FastAPI 호출 실패]: {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 피드백 요청 실패", e);
        }
    }
}
