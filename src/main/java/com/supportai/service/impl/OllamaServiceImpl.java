package com.supportai.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.supportai.config.AiProperties;
import com.supportai.service.OllamaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements OllamaService {

    private final AiProperties aiProperties;

    @Override
    public String generate(String prompt) {

        RestClient restClient =
                RestClient.builder()
                        .baseUrl(aiProperties.getBaseUrl())
                        .build();

        Map<String, Object> request =
                new HashMap<>();

        request.put(
                "model",
                aiProperties.getModel()
        );

        request.put(
                "prompt",
                prompt
        );

        request.put(
                "stream",
                false
        );

        Map<?, ?> response =
                restClient.post()
                        .uri("/api/generate")
                        .body(request)
                        .retrieve()
                        .body(Map.class);

        if (response == null
                || response.get("response") == null) {

            throw new IllegalStateException(
                    "Invalid response received from Ollama"
            );
        }

        return response.get("response")
                .toString()
                .trim();
    }
}