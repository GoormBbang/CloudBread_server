package com.cloudbread.domain.chat.nutrients.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiClientsConfig {
    @Bean
    public WebClient aiChatClient(
            WebClient.Builder builder,
            @Value("${ai.chat.base-url}") String baseUrl // ex) http://chatbot-server-svc.ai-services.svc.cluster.local
    ) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
