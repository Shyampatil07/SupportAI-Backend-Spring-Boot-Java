package com.supportai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportai.config.AiProperties;
import com.supportai.dto.AiClassificationResponse;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.service.AiClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiClassificationServiceImpl implements AiClassificationService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public AiClassificationResponse classifyTicket(
            String title,
            String description) {

        RestClient restClient = RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .build();

        String prompt = """
                You are an AI support ticket classifier.

                Classify the following support ticket.

                Allowed categories:
                TECHNICAL
                BILLING
                ACCOUNT
                PRODUCT
                GENERAL

                Allowed priorities:
                LOW
                MEDIUM
                HIGH
                CRITICAL

                Return ONLY valid JSON in this exact structure:

                {
                  "category": "TECHNICAL",
                  "priority": "HIGH",
                  "reason": "Short explanation"
                }

                Ticket title:
                %s

                Ticket description:
                %s
                """.formatted(title, description);

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        Map<?, ?> response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        try {

            String aiResponse =
                    (String) response.get("response");

            JsonNode json =
                    objectMapper.readTree(aiResponse);

            return AiClassificationResponse.builder()
                    .category(
                            TicketCategory.valueOf(
                                    json.get("category")
                                            .asText()
                                            .toUpperCase()
                            )
                    )
                    .priority(
                            TicketPriority.valueOf(
                                    json.get("priority")
                                            .asText()
                                            .toUpperCase()
                            )
                    )
                    .reason(
                            json.get("reason").asText()
                    )
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse AI classification response",
                    e
            );
        }
    }
}