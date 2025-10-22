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
    public String generateFeedback(Long userId) {
        log.info("[Feedback] FastAPI 피드백 요청 시작 userId={}", userId);

        // 1. 사용자 컨텍스트 구성 (FastAPI 전송용)
        UserFeedbackRequestDto.AiUserRequest userContext =
                userContextBuilder.buildFeedbackUserRequest(userId);

        // 2. 오늘의 영양 밸런스 조회
        LocalDate today = LocalDate.now();
        List<UserDailyNutrition> todayBalanceList =
                userDailyNutritionRepository.findByUserIdAndDate(userId, today);

        // 3. 밸런스가 없으면 자동 계산 시도
        if (todayBalanceList.isEmpty()) {
            log.warn("오늘의 영양 밸런스 데이터가 없습니다. 자동 계산을 시도합니다. userId={}", userId);
            try {
                // 영양 밸런스 자동 계산 및 저장
                userNutritionStatsService.getNutritionBalance(userId, today);

                // 다시 조회
                todayBalanceList = userDailyNutritionRepository.findByUserIdAndDate(userId, today);

                if (todayBalanceList.isEmpty()) {
                    throw new IllegalStateException("오늘의 영양 밸런스 계산에 실패했습니다. userId=" + userId);
                }

            } catch (Exception e) {
                log.error("[자동 밸런스 계산 실패] userId={}, error={}", userId, e.getMessage(), e);
                throw new IllegalStateException("오늘의 영양 밸런스 데이터가 없습니다. userId=" + userId);
            }
        }

        // 4. FastAPI 요청 DTO 생성
        FeedbackRequestDto requestDto = FeedbackRequestDto.of(userContext, todayBalanceList);

        // 5. FastAPI 호출
        ResponseEntity<String> response = fastApiFeedbackClient.requestRawFeedback(requestDto);
        log.info("[FastAPI 원본 응답]: {}", response.getBody());

        // 6. 응답 처리 및 DB 저장
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            FeedbackResponseDto feedbackResponse =
                    mapper.readValue(response.getBody(), FeedbackResponseDto.class);

            if (feedbackResponse.isSuccess() && feedbackResponse.getResult() != null) {
                String content = feedbackResponse.getResult().getFeedbackSummary();
                String feedbackDateStr = feedbackResponse.getResult().getFeedbackDate();

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID"));

                Feedback feedback = Feedback.builder()
                        .user(user)
                        .content(content)
                        .feedbackDate(LocalDate.parse(feedbackDateStr))
                        .build();

                feedbackRepository.save(feedback);
                log.info("[피드백 저장 완료] id={}, userId={}, createdAt={}",
                        feedback.getId(), userId, feedback.getCreatedAt());
            }
        } catch (Exception e) {
            log.error("[FastAPI 응답 처리 실패]: {}", e.getMessage(), e);
        }

        return response.getBody();
    }
}