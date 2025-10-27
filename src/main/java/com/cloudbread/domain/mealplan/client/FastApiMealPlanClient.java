package com.cloudbread.domain.mealplan.client;

import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.fastapi.url}")
    private String fastApiUrl;

    public MealPlanResponseDto requestMealPlan(MealPlanRequestDto requestDto) {
        String endpoint = fastApiUrl + "/api/food/api/v1/recommend";

        try {
            //log.info("[FastAPI 요청 시작] URL={}, request={}", endpoint, requestDto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MealPlanRequestDto> entity = new HttpEntity<>(requestDto, headers);

            // FastAPI POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 원본 응답 JSON 로그
            String body = response.getBody();
            log.info("[FastAPI RAW BODY] {}", body); // ← 여기에 첫 번째 로그

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode root = mapper.readTree(body);
            JsonNode resultNode = root.get("result");

            if (resultNode == null || resultNode.isNull()) {
                throw new RuntimeException("FastAPI 응답에 result 필드가 없습니다.");
            }

            // resultNode → DTO 변환
            MealPlanResponseDto dto = mapper.treeToValue(resultNode, MealPlanResponseDto.class);

            // 파싱된 DTO 로그
            log.info("[파싱 완료 DTO] {}", mapper.writeValueAsString(dto)); // ← 여기에 두 번째 로그

            return dto;

        } catch (Exception e) {
            log.error("[FastAPI 호출 실패] {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 서버 통신 중 오류 발생", e);
        }
    }

}
