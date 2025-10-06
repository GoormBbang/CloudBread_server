////package com.cloudbread.domain.mealplan.client;
////
////import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
////import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.http.*;
////import org.springframework.stereotype.Component;
////import org.springframework.web.client.HttpStatusCodeException;
////import org.springframework.web.client.RestTemplate;
////
////import java.util.List;
////
////@Slf4j
////@Component
////@RequiredArgsConstructor
////public class FastApiMealPlanClient {
////
////    private final RestTemplate restTemplate = new RestTemplate();
////
////    // ✅ FastAPI 서버 URL (application.yml에서 주입받음)
////    @Value("${external.fastapi.url}")
////    private String fastApiUrl;
////
////    /**
////     * FastAPI에 유저 정보를 전달해 AI 추천 식단 생성 요청
////     */
////    public MealPlanResponseDto requestMealPlan(MealPlanRequestDto requestDto) {
////        String url = fastApiUrl + "/api/v1/recommend";
////
////        try {
////            log.info("[FastAPI 요청 시작] URL: {}", url);
////
////            // ✅ 요청 헤더 설정
////            HttpHeaders headers = new HttpHeaders();
////            headers.setContentType(MediaType.APPLICATION_JSON);
////            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
////
////            // ✅ RequestEntity 생성
////            HttpEntity<MealPlanRequestDto> entity = new HttpEntity<>(requestDto, headers);
////
////            // ✅ FastAPI POST 요청
////            ResponseEntity<MealPlanResponseDto> response = restTemplate.exchange(
////                    url,
////                    HttpMethod.POST,
////                    entity,
////                    MealPlanResponseDto.class
////            );
////
////            log.info("[FastAPI 응답 성공] status: {}", response.getStatusCode());
////            return response.getBody();
////
////        } catch (HttpStatusCodeException e) {
////            // FastAPI 쪽 오류 (ex: 422, 500 등)
////            log.error("[FastAPI 응답 에러] status={}, body={}",
////                    e.getStatusCode(), e.getResponseBodyAsString());
////            throw new RuntimeException("FastAPI 요청 실패: " + e.getMessage(), e);
////        } catch (Exception e) {
////            // 네트워크/파싱 에러
////            log.error("[FastAPI 호출 실패] {}", e.getMessage(), e);
////            throw new RuntimeException("FastAPI 서버 통신 중 오류 발생", e);
////        }
////    }
////}
//package com.cloudbread.domain.mealplan.client;
//
//import com.cloudbread.domain.mealplan.dto.MealPlanRequestDto;
//import com.cloudbread.domain.mealplan.dto.MealPlanResponseDto;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class FastApiMealPlanClient {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @Value("${external.fastapi.url}")
//    private String fastApiUrl;
//
//    /**
//     * FastAPI에 유저 정보를 전달해 AI 추천 식단 생성 요청
//     */
//    public MealPlanResponseDto requestMealPlan(MealPlanRequestDto requestDto) {
//        String endpoint = fastApiUrl + "/api/food/api/v1/recommend";
//
//        try {
//            log.info("[FastAPI 요청 시작] URL={}, request={}", endpoint, requestDto);
//
//            // ✅ 요청 헤더 설정
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<MealPlanRequestDto> entity = new HttpEntity<>(requestDto, headers);
//
//            // ✅ FastAPI POST 요청 (응답을 String으로 받음)
//            ResponseEntity<String> response = restTemplate.exchange(
//                    endpoint,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//            );
//
//            String body = response.getBody();
//            log.info("[FastAPI 응답 RAW] {}", body);
//
//            // ✅ ObjectMapper 설정 (알 수 없는 필드 무시)
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            // ✅ result 필드만 추출
//            JsonNode root = mapper.readTree(body);
//            JsonNode resultNode = root.get("result");
//
//            if (resultNode == null || resultNode.isNull()) {
//                throw new RuntimeException("FastAPI 응답에 result 필드가 없습니다.");
//            }
//
//            // ✅ resultNode → MealPlanResponseDto 변환
//            MealPlanResponseDto dto = mapper.treeToValue(resultNode, MealPlanResponseDto.class);
//
//            log.info("[FastAPI 응답 변환 완료] planId={}, sections size={}",
//                    dto.getPlanId(),
//                    dto.getSections() != null ? dto.getSections().size() : 0);
//
//            return dto;
//
//        } catch (Exception e) {
//            log.error("[FastAPI 호출 실패] {}", e.getMessage(), e);
//            throw new RuntimeException("FastAPI 서버 통신 중 오류 발생", e);
//        }
//    }
//}
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

            // ✅ FastAPI POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // ✅ 원본 응답 JSON 로그
            String body = response.getBody();
            log.info("[FastAPI RAW BODY] {}", body); // ← 여기에 첫 번째 로그

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode root = mapper.readTree(body);
            JsonNode resultNode = root.get("result");

            if (resultNode == null || resultNode.isNull()) {
                throw new RuntimeException("FastAPI 응답에 result 필드가 없습니다.");
            }

            // ✅ resultNode → DTO 변환
            MealPlanResponseDto dto = mapper.treeToValue(resultNode, MealPlanResponseDto.class);

            // ✅ 파싱된 DTO 로그
            log.info("[파싱 완료 DTO] {}", mapper.writeValueAsString(dto)); // ← 여기에 두 번째 로그

//            log.info("[FastAPI 응답 변환 완료] planId={}, sections={}",
//                    dto.getPlanId(),
//                    dto.getSections() != null ? dto.getSections().size() : 0);

            return dto;

        } catch (Exception e) {
            log.error("[FastAPI 호출 실패] {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 서버 통신 중 오류 발생", e);
        }
    }

}
