package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.feedback.client.FastApiFeedbackClient;
import com.cloudbread.domain.feedback.domain.entity.Feedback;
import com.cloudbread.domain.feedback.domain.repository.FeedbackRepository;
import com.cloudbread.domain.feedback.dto.FeedbackResponseDto;
import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;
import com.cloudbread.domain.feedback.dto.UserFeedbackRequestDto;
import com.cloudbread.domain.nutrition.application.UserNutritionStatsService;
import com.cloudbread.domain.nutrition.domain.entity.UserDailyNutrition;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserDailyNutritionRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final UserContextBuilder userContextBuilder;
    private final UserDailyNutritionRepository userDailyNutritionRepository;
    private final FastApiFeedbackClient fastApiFeedbackClient;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final UserNutritionStatsService userNutritionStatsService;

    @Override
    @Transactional
    public BaseResponse<FeedbackResponseDto.Result> generateFeedback(Long userId) {
        log.info("[Feedback] FastAPI 피드백 요청 시작 userId={}", userId);

        // 1. 사용자 컨텍스트 구성
        UserFeedbackRequestDto.AiUserRequest userContext =
                userContextBuilder.buildFeedbackUserRequest(userId);

        // 2. 오늘 날짜 (KST 기준)
        LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<UserDailyNutrition> todayBalanceList =
                userDailyNutritionRepository.findByUserIdAndDate(userId, todayKst);

        // 3. 오늘의 영양 데이터 없으면 계산 시도
        if (todayBalanceList.isEmpty()) {
            log.warn("오늘의 영양 밸런스 데이터가 없습니다. 자동 계산 시도: userId={}", userId);
            try {
                userNutritionStatsService.getNutritionBalance(userId, todayKst);
                todayBalanceList = userDailyNutritionRepository.findByUserIdAndDate(userId, todayKst);
                if (todayBalanceList.isEmpty()) {
                    throw new IllegalStateException("영양 밸런스 계산 실패 userId=" + userId);
                }
            } catch (Exception e) {
                log.error("[자동 계산 실패] userId={}, error={}", userId, e.getMessage(), e);
                throw new IllegalStateException("오늘의 영양 밸런스 데이터가 없습니다.", e);
            }
        }

        // 4. FastAPI 요청 DTO 생성
        FeedbackRequestDto requestDto = FeedbackRequestDto.of(userContext, todayBalanceList);

        // 5. FastAPI 호출
        ResponseEntity<String> response = fastApiFeedbackClient.requestRawFeedback(requestDto);
        log.info("[FastAPI 원본 응답]: {}", response.getBody());

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            FeedbackResponseDto fastApiResponse =
                    mapper.readValue(response.getBody(), FeedbackResponseDto.class);

            // FastAPI BaseResponse에서 내부 result만 추출
            FeedbackResponseDto.Result innerResult = fastApiResponse.getResult();

            if (innerResult != null) {
                String content = innerResult.getFeedbackSummary();
                String feedbackDateStr = innerResult.getFeedbackDate();

                LocalDate feedbackDate = (feedbackDateStr != null && !feedbackDateStr.isBlank())
                        ? LocalDate.parse(feedbackDateStr)
                        : todayKst;

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID"));

                Feedback feedback = Feedback.builder()
                        .user(user)
                        .content(content)
                        .feedbackDate(feedbackDate)
                        .build();

                feedbackRepository.save(feedback);
                log.info("[피드백 저장 완료] id={}, userId={}, feedbackDate={}",
                        feedback.getId(), userId, feedbackDate);
            }

            // BaseResponse로 감싸되 FastAPI의 BaseResponse는 제외
            return BaseResponse.onSuccess(SuccessStatus.FEEDBACK_SUCCESS, innerResult);

        } catch (Exception e) {
            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage());
            throw new IllegalStateException("FastAPI 응답 처리 중 오류 발생", e);
        }


//    @Override
//    public String generateFeedback(Long userId) {
//        log.info("[Feedback] FastAPI 피드백 요청 시작 userId={}", userId);
//
//        UserFeedbackRequestDto.AiUserRequest userContext =
//                userContextBuilder.buildFeedbackUserRequest(userId);
//
//        List<UserDailyNutrition> todayBalanceList =
//                userDailyNutritionRepository.findByUserIdAndDate(userId, LocalDate.now());
//
//        if (todayBalanceList.isEmpty()) {
//            throw new IllegalStateException("오늘의 영양 밸런스 데이터가 없습니다. userId=" + userId);
//        }
//
//        FeedbackRequestDto requestDto = FeedbackRequestDto.of(userContext, todayBalanceList);
//
//        // FastAPI 호출
//        ResponseEntity<String> response = fastApiFeedbackClient.requestRawFeedback(requestDto);
//        log.info("[FastAPI 원본 응답]: {}", response.getBody());
//
//                        .feedbackDate(planDate)
//                        .build();
//
//                feedbackRepository.save(feedback);
//                log.info("[피드백 저장 완료] id={}, userId={}, createdAt={}",
//                        feedback.getId(), userId, feedback.getCreatedAt());
//            } else {
//                log.warn("[FastAPI 응답 실패 or 비정상 응답]: {}", feedbackResponse);
//            }
//
//        } catch (Exception e) {
//            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage(), e);
//            throw new IllegalStateException("FastAPI 피드백 응답 처리 중 오류가 발생했습니다.");
//        }
//
//        return feedbackResponse;
//    }
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.registerModule(new JavaTimeModule());
//            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//
//            FeedbackResponseDto feedbackResponse =
//                    mapper.readValue(response.getBody(), FeedbackResponseDto.class);
//
//            if (feedbackResponse.isSuccess() && feedbackResponse.getResult() != null) {
//                String content = feedbackResponse.getResult().getFeedbackSummary();

//                String feedbackDateStr = feedbackResponse.getResult().getFeedbackDate();

//             //   String feedbackDateStr = feedbackResponse.getResult().getFeedbackDate();
//
//                ZoneId KST = ZoneId.of("Asia/Seoul");
//                LocalDate planDate = LocalDate.now(KST);
//                log.info("ai 추천 식단, planDate={}", planDate);
//

//
//                User user = userRepository.findById(userId)
//                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID"));
//
//                Feedback feedback = Feedback.builder()
//                        .user(user)
//                        .content(content)

//                        .feedbackDate(LocalDate.parse(feedbackDateStr))
//                        .feedbackDate(planDate)
//                        .build();
//
//                feedbackRepository.save(feedback);
//                log.info("[피드백 저장 완료] id={}, userId={}, createdAt={}",
//                        feedback.getId(), userId, feedback.getCreatedAt());
//            }
//        } catch (Exception e) {
//            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage());
//            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage(), e);
//        }
//
//        return response.getBody();
//    }

}
}
