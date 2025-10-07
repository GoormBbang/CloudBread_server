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

        // system_prompt 생성 (이미지 항목 제거)
        String systemPrompt = buildSystemPrompt(ctx);

        // FastAPI 프록시 호출
        AiChatRequest aiReq = new AiChatRequest();
        aiReq.setSession_id(req.getSessionId());
        aiReq.setMessage(req.getMessage());
        aiReq.setSystem_prompt(systemPrompt);

        AiChatResponse aiRes = aiChatClient.post()
                .uri("/api/chatbot/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiReq) // Post요청의 requestBody
                .retrieve()
                .bodyToMono(AiChatResponse.class) // Response 받아서 -> AiChatResponse 에
            //    .block(Duration.ofSeconds(30));
                .block(); // 테스트용, 인자없이 무한대기

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


    private String buildSystemPrompt(SessionContext ctx) {
        try {
            String profileJson = om.writeValueAsString(ctx.userProfile());
            Map<String,Object> foodJson = new LinkedHashMap<>();
            foodJson.put("food_id", ctx.food().getFoodId());
            foodJson.put("name", ctx.food().getName());
            foodJson.put("serving", ctx.food().getServing());
            foodJson.put("calories", ctx.food().getCalories());
            foodJson.put("nutrients", ctx.food().getNutrients());
            String foodJsonStr = om.writeValueAsString(foodJson);

            return """
                당신은 한국어로 답하는 영양 코치입니다.
                아래 JSON 컨텍스트를 바탕으로 과학적 근거 기반, 간결하고 친절하게 답변하세요.
                단위는 반드시 표기하고, 주의/대체 식재료/조리 팁을 제시하세요.

                [USER_PROFILE]
                %s

                [FOOD]
                %s

                규칙:
                - 건강/식이/알레르기와 충돌 시 먼저 경고.
                - 불확실하면 추가 질문으로 명확화.
                - "요약 → 구체 팁 → 대체 조리법/재료" 순서로 답변.
                """.formatted(profileJson, foodJsonStr);
        } catch (Exception e) {
            throw new IllegalStateException("system_prompt build failed", e);
        }
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


}
