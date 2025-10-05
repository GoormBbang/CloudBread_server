package com.cloudbread.domain.mealplan.client;

import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiMealPlanClient {

    @Value("${external.fastapi.mealplan-url}")
    private String fastApiMealPlanUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MealPlanResponseDto requestMealPlan(MealPlanRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MealPlanRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            //log.info("🌐 [요청 URL] {}", fastApiMealPlanUrl);
            //log.info("📤 [요청 BODY] {}", objectMapper.writeValueAsString(requestDto));

            // ✅ 응답을 MealPlanResponseDto.class로 바로 받기
            ResponseEntity<MealPlanResponseDto> response = restTemplate.exchange(
                    fastApiMealPlanUrl,
                    HttpMethod.POST,
                    entity,
                    MealPlanResponseDto.class
            );

            // ✅ 로그로 전체 응답 확인
            //log.info("✅ [응답 상태] {}", response.getStatusCode());
            //log.info("🍱 [응답 BODY] {}", objectMapper.writeValueAsString(response.getBody()));

            return response.getBody();

        } catch (Exception e) {
            log.error("FastAPI 통신 실패", e);
            throw new RuntimeException("FastAPI 통신 실패", e);
        }
    }
}
