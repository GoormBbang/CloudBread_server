package com.cloudbread.domain.chat.main.application;

import com.cloudbread.domain.chat.main.application.session_store.AiSessionContext;
import com.cloudbread.domain.chat.main.application.session_store.AiSessionStore;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatRequestGen;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatResponseGen;
import com.cloudbread.domain.chat.main.exception.InvalidTopicException;
import com.cloudbread.domain.chat.main.exception.MissingTopicException;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfile;
import com.cloudbread.domain.chat.nutrients.application.user_profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AiGeneralChatServiceImpl implements AiGeneralChatService {
    private final UserProfileService userProfileService;
    private final AiSessionStore sessionStore;
    private static final long SESSION_TTL_SECONDS = 60 * 60 * 2; // 2시간

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
}
