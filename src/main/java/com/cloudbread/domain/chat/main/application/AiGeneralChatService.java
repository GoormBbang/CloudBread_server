package com.cloudbread.domain.chat.main.application;


import com.cloudbread.domain.chat.main.dto.AiGeneralChatRequestGen;
import com.cloudbread.domain.chat.main.dto.AiGeneralChatResponseGen;

public interface AiGeneralChatService {
    AiGeneralChatResponseGen.SessionCreatedGen createSession(Long userId, AiGeneralChatRequestGen.CreateSessionGen req);
}
