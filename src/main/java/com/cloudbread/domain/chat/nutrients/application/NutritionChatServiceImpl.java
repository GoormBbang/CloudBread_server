package com.cloudbread.domain.chat.nutrients.application;

import com.cloudbread.domain.chat.nutrients.application.session_store.InMemorySessionStore;
import com.cloudbread.domain.chat.nutrients.application.session_store.SessionContext;
import com.cloudbread.domain.chat.nutrients.application.session_store.SessionStore;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfile;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfileService;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatRequest;
import com.cloudbread.domain.chat.nutrients.dto.NutritionChatResponse;
import com.cloudbread.domain.chat.nutrients.dto.ai.AiChatRequest;
import com.cloudbread.domain.chat.nutrients.dto.ai.AiChatResponse;
import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NutritionChatServiceImpl implements NutritionChatService {
    private final FoodRepository foodRepository;
    private final FoodNutrientRepository foodNutrientRepository;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final SessionStore sessionStore;
    private final WebClient aiChatClient;
    private static final long SESSION_TTL_SECONDS = 60 * 60 * 2; // 2시간
    private final ObjectMapper om = new ObjectMapper();
    @Override
    public NutritionChatResponse.SessionCreated createSession(Long userId, NutritionChatRequest.CreateSession req) {
        if (req.getFoodId() == null) throw new IllegalArgumentException("foodId is required");

        // Food Summary 조립
        Food food = foodRepository.findById(req.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("food not found: " + req.getFoodId()));
        NutritionChatResponse.SelectedFoodSummary foodSummary = buildFoodSummary(food);

        // 사용자 프로필 집계
        UserProfile profile = userProfileService.getProfile(userId);

        // 세션 생성 & 저장
        String sessionId = InMemorySessionStore.newSessionId();
        SessionContext ctx = new SessionContext(userId, foodSummary, profile, Instant.now());
        sessionStore.put(sessionId, ctx, SESSION_TTL_SECONDS);

        // 로그
        SessionContext sct = sessionStore.get(sessionId);
        if (sct == null){
            log.error("[NC] 세션 저장 실패, sessionId : {}", sessionId);
        } else {
            log.info("[NC] 세션 저장 성공, userId : {}, foodSummary: {}, profile : {} ",
                    sct.userId(), sct.food(), sct.userProfile());
        }


        return NutritionChatResponse.SessionCreated.builder()
                .sessionId(sessionId)
                .selectedFood(foodSummary)
                .build();
    }

    @Override
    public NutritionChatResponse.Message send(Long userId, NutritionChatRequest.SendMessage req) {
        // 세션 컨텍스트 복원
        SessionContext ctx = sessionStore.require(userId, req.getSessionId());

        // FastAPI에 보낼 context 오브젝트 구성 (system_prompt 쓰지 않음)
        Map<String, Object> context = buildContextForAi(userId, ctx);

        // 요청 DTO 세팅
        AiChatRequest aiReq = new AiChatRequest();
        aiReq.setSession_id(req.getSessionId());
        aiReq.setMessage(req.getMessage());
        aiReq.setContext(context);

        // == 로그용 코드 ==
        final String path = "/api/chatbot/chat";
        final String reqJson = toPrettyJson(aiReq);
        log.info("[NC→AI] HTTP Request\nMETHOD: POST\nURL: {}\nHEADERS: accept=application/json, content-type=application/json\nBODY:\n{}", path, reqJson);

        // Fast api 호출
        AiChatResponse aiRes = aiChatClient.post()
                .uri("/api/chatbot/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiReq) // FastAPI에게 보낼 requestBody
                .retrieve()
                .bodyToMono(AiChatResponse.class)
                .block(); // 테스트 중이면 무제한, 운영 시 .block(Duration.ofSeconds(30)) 권장

        // 매핑
        List<NutritionChatResponse.HistoryItem> history = aiRes.getMessage_history().stream()
                .map(m -> NutritionChatResponse.HistoryItem.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();

        return NutritionChatResponse.Message.builder()
                .sessionId(aiRes.getSession_id())
                .response(aiRes.getResponse())
                .history(history)
                .build();
    }


    /** BE → FastAPI context 변환 (topic 고정: FOOD_INFO) */
    private Map<String, Object> buildContextForAi(Long userId, SessionContext sctx) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("topic", "FOOD_INFO"); // 영양분석 후 챗봇은 FOOD_INFO로 고정이니깐

        // user_profile
        Map<String, Object> up = new LinkedHashMap<>();

        up.put("user_id", userId);
        up.put("pregnancy_week", calcPregnancyWeek(userId)); // null 허용
        up.put("health_conditions", sctx.userProfile().healthConditions());
        up.put("diets", sctx.userProfile().diets());
        up.put("allergies", sctx.userProfile().allergies());
        root.put("user_profile", up);

        // food
        Map<String, Object> food = new LinkedHashMap<>();

        food.put("id", sctx.food().getFoodId());
        food.put("name", sctx.food().getName());
        food.put("serving", sctx.food().getServing());
        food.put("calories", sctx.food().getCalories());
        // Nutrients(Map<String, NutrientValue>) 그대로 직렬화
        food.put("nutrients", sctx.food().getNutrients());
        root.put("food", food);

        return root;
    }

    /** due_date(출산예정일) 기준 임신 주차 계산: LMP = dueDate - 40주, GA = floor((today - LMP)/7), 0~42로 클램프
     *  LMP : 마지막 월경 시작일 계산
     *  임신기간 : 마지막 월경일로부터 40주
     * */
    private Integer calcPregnancyWeek(Long userId) {
        return userRepository.findById(userId)
                .map(User::getDueDate)
                .map(dueDate -> {
                    LocalDate today = LocalDate.now();
                    LocalDate lmp = dueDate.minusWeeks(40); // 마지막 월경일
                    long days = ChronoUnit.DAYS.between(lmp, today); // 현재까지의 일수
                    int weeks = (int) Math.floor(days / 7.0); // 임신주차 게산

                    // 예외처리
                    if (weeks < 0) weeks = 0;
                    if (weeks > 42) weeks = 42;
                    return weeks;
                })
                .orElse(null);
    }

    // == helpers ==
    private NutritionChatResponse.SelectedFoodSummary buildFoodSummary(Food food) {
        Map<String, NutritionChatResponse.NutrientValue> nutrients = new LinkedHashMap<>();
        BigDecimal calories = food.getCalories(); // 별도 필드

        List<FoodNutrient> list =
                foodNutrientRepository.findByFoodId(food.getId());

        for (FoodNutrient fn : list) {
            String key = normalizeKey(fn.getNutrient().getName());

            if ("calories".equals(key)) continue;

            String unit = unitSymbol(fn.getNutrient().getUnit());
            nutrients.put(key, NutritionChatResponse.NutrientValue.builder()
                    .value(fn.getValue())
                    .unit(unit)
                    .build());
        }

        return NutritionChatResponse.SelectedFoodSummary.builder()
                .foodId(food.getId())
                .name(food.getName())
                .serving(food.getSourceName()) // 프로젝트에서 서빙명을 여기 담아왔음
                .calories(calories)
                .nutrients(nutrients)
                .build();
    }

    private String normalizeKey(String s) { return (s == null) ? null : s.toLowerCase(); }

    private String unitSymbol(Unit u) { return (u == null) ? null : u.name().toLowerCase(); }

    private String toPrettyJson(Object o) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }


}
