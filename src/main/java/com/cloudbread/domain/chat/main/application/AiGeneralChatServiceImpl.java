package com.cloudbread.domain.chat.main.application;

import com.cloudbread.domain.chat.main.application.session_store.AiSessionContext;
import com.cloudbread.domain.chat.main.application.session_store.AiSessionStore;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatRequestGen;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatResponseGen;
import com.cloudbread.domain.chat.main.exception.InvalidTopicException;
import com.cloudbread.domain.chat.main.exception.MissingTopicException;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfile;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfileService;
import com.cloudbread.domain.chat.nutrients.dto.ai.AiChatRequest;
import com.cloudbread.domain.chat.nutrients.dto.ai.AiChatResponse;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
public class AiGeneralChatServiceImpl implements AiGeneralChatService {
    private final UserProfileService userProfileService;
    private final AiSessionStore sessionStore;
    private final UserRepository userRepository;
    private final WebClient aiChatClient;
    private static final long SESSION_TTL_SECONDS = 60 * 60 * 2; // 2시간
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public AiGeneralChatResponseGen.SessionCreatedGen createSession(Long userId, AiGeneralChatRequestGen.CreateSessionGen req) {
        String topic = normalizeTopicOrThrow(req.getTopic());

        UserProfile profile = userProfileService.getProfile(userId);

        String sessionId = AiSessionStore.newSessionId();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(SESSION_TTL_SECONDS);

        AiSessionContext ctx = new AiSessionContext(userId, topic, profile, now);
        sessionStore.put(sessionId, ctx, SESSION_TTL_SECONDS);


        log.info("[AI] 세션 생성 성공: sessionId={}, userId={}, topic={}, expiresAt={}", sessionId, userId, topic, expiresAt);

        return AiGeneralChatResponseGen.SessionCreatedGen.builder()
                .sessionId(sessionId)
                .topic(topic)
                .expiresAt(expiresAt.toString())
                .build();
    }

    @Override
    public AiGeneralChatResponseGen.MessageGen send(Long userId, AiGeneralChatRequestGen.SendMessageGen req) {
        // 세션 컨텍스트 복원
        AiSessionContext ctx = sessionStore.require(userId, req.getSessionId());

        // 요청 topic이 들어오면 세션 topic 갱신(전환 가능)
        String effectiveTopic = normalizeTopicOrThrow(req.getTopic());
        if (!effectiveTopic.equals(ctx.topic())) {
            sessionStore.updateTopic(req.getSessionId(), effectiveTopic);
            log.info("[AI] 세션 topic 갱신: sessionId={}, {} -> {}", req.getSessionId(), ctx.topic(), effectiveTopic);
        }

        // context 구성 (system_prompt 사용 안 함)
        Map<String, Object> context = buildContextForAi(userId, effectiveTopic, ctx.userProfile());

        // FastAPI 요청 DTO
        AiChatRequest aiReq = new AiChatRequest();
        aiReq.setSession_id(req.getSessionId());
        aiReq.setMessage(req.getMessage());
        aiReq.setContext(context);

        // == 전송 로그 (URL/Headers/Body) ==
        final String path = "/api/chatbot/chat";
        final String reqJson = toPrettyJson(aiReq);
        log.info("[AI-OUT] HTTP Request\nMETHOD: POST\nURL: {}\nHEADERS: accept=application/json, content-type=application/json\nBODY:\n{}",
                path, reqJson);
        final long t0 = System.nanoTime();

        // 5) 호출
        AiChatResponse aiRes = aiChatClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiReq)
                .retrieve()
                .bodyToMono(AiChatResponse.class)
                .block(); // 운영시 .block(Duration.ofSeconds(30)) 권장

        // 6) TTL 갱신
        sessionStore.touch(req.getSessionId(), SESSION_TTL_SECONDS);

        // 7) 매핑
        List<AiGeneralChatResponseGen.HistoryItemGen> history = aiRes.getMessage_history().stream()
                .map(m -> AiGeneralChatResponseGen.HistoryItemGen.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();

        return AiGeneralChatResponseGen.MessageGen.builder()
                .sessionId(aiRes.getSession_id())
                .topic(effectiveTopic)
                .response(aiRes.getResponse())
                .history(history)
                .build();
    }

    private Map<String, Object> buildContextForAi(Long userId, String topic, UserProfile profile) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("topic", topic);

        Map<String, Object> up = new LinkedHashMap<>();
        up.put("user_id", userId);
        up.put("pregnancy_week", calcPregnancyWeek(userId)); // null 허용
        up.put("health_conditions", profile.healthConditions());
        up.put("diets", profile.diets());
        up.put("allergies", profile.allergies());
        root.put("user_profile", up);

        return root;
    }

    /** due_date 기반 임신 주차: LMP = dueDate - 40주, GA = floor((today - LMP)/7), 0~42 클램프 */
    private Integer calcPregnancyWeek(Long userId) {
        return userRepository.findById(userId)
                .map(User::getDueDate)
                .map(dueDate -> {
                    LocalDate today = LocalDate.now();
                    LocalDate lmp = dueDate.minusWeeks(40);
                    long days = ChronoUnit.DAYS.between(lmp, today);
                    int weeks = (int) Math.floor(days / 7.0);
                    if (weeks < 0) weeks = 0;
                    if (weeks > 42) weeks = 42;
                    return weeks;
                })
                .orElse(null);
    }

    private String normalizeTopicOrThrow(String topic) {
        if (topic == null) {
            throw new MissingTopicException();
        };
        String t = topic.trim().toUpperCase();
        if (!List.of("FOOD_INFO","PREGNANCY_DRUG","PREGNANCY","FREE").contains(t)) {
            throw new InvalidTopicException();
        }
        return t;
    }


    private String toPrettyJson(Object o) {
        try { return om.writerWithDefaultPrettyPrinter().writeValueAsString(o); }
        catch (Exception e) { return String.valueOf(o); }
    }
}
