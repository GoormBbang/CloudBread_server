package com.cloudbread.domain.feedback.application;

import com.cloudbread.domain.feedback.client.FastApiFeedbackClient;
import com.cloudbread.domain.feedback.domain.entity.Feedback;
import com.cloudbread.domain.feedback.domain.repository.FeedbackRepository;
import com.cloudbread.domain.feedback.dto.FeedbackResponseDto;
import com.cloudbread.domain.feedback.dto.FeedbackRequestDto;
import com.cloudbread.domain.feedback.dto.UserFeedbackRequestDto;
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

    @Override
    public BaseResponse<FeedbackResponseDto.Result> generateFeedback(Long userId) {
        log.info("[Feedback] FastAPI 피드백 요청 시작 userId={}", userId);

        UserFeedbackRequestDto.AiUserRequest userContext =
                userContextBuilder.buildFeedbackUserRequest(userId);

        LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<UserDailyNutrition> todayBalanceList =
                userDailyNutritionRepository.findByUserIdAndDate(userId, todayKst);

        if (todayBalanceList.isEmpty()) {
            throw new IllegalStateException("오늘의 영양 밸런스 데이터가 없습니다. userId=" + userId);
        }

        FeedbackRequestDto requestDto = FeedbackRequestDto.of(userContext, todayBalanceList);
        ResponseEntity<String> response = fastApiFeedbackClient.requestRawFeedback(requestDto);
        log.info("📬 [FastAPI 원본 응답]: {}", response.getBody());

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            FeedbackResponseDto fastApiResponse =
                    mapper.readValue(response.getBody(), FeedbackResponseDto.class);

            // FastAPI의 result만 추출 (BaseResponse 제외)
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
//
//                User user = userRepository.findById(userId)
//                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID"));
//
//                Feedback feedback = Feedback.builder()
//                        .user(user)
//                        .content(content)
//                        .feedbackDate(LocalDate.parse(feedbackDateStr))
//                        .build();
//
//                feedbackRepository.save(feedback);
//                log.info("[피드백 저장 완료] id={}, userId={}, createdAt={}",
//                        feedback.getId(), userId, feedback.getCreatedAt());
//            }
//        } catch (Exception e) {
//            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage());
//        }
//
//        return response.getBody();
//    }
}
