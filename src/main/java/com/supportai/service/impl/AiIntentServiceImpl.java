package com.supportai.service.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportai.config.AiProperties;
import com.supportai.dto.AiIntentResponse;
import com.supportai.entity.AiIntent;
//import com.supportai.enums.AiIntent;
import com.supportai.service.AiIntentService;
import com.supportai.service.OllamaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiIntentServiceImpl implements AiIntentService {

    private final AiProperties aiProperties;
//    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper;

    @Override
    public AiIntentResponse detectIntent(
            String message,
            String conversationContext) {

        /*
         * First handle very clear UPDATE_TICKET requests
         * deterministically.
         *
         * This prevents Ollama from incorrectly classifying
         * "change/update/modify/rename" requests as ADD_COMMENT.
         */
        AiIntentResponse explicitUpdate =
                detectExplicitUpdate(message);

        if (explicitUpdate != null) {
            System.out.println("========== EXPLICIT UPDATE DETECTED ==========");
            System.out.println("Intent       : " + explicitUpdate.getIntent());
            System.out.println("Ticket ID    : " + explicitUpdate.getTicketId());
            System.out.println("Update Field : " + explicitUpdate.getUpdateField());
            System.out.println("Update Value : " + explicitUpdate.getUpdateValue());
            System.out.println("==============================================");

            return explicitUpdate;
        }
        // 2. Explicit comment request
        AiIntentResponse explicitComment =
                detectExplicitComment(message);

        if (explicitComment != null) {
            return explicitComment;
        }

        /*
         * If it is not an obvious update request,
         * let Ollama determine the intent.
         */
        String prompt =
                buildPrompt(message, conversationContext);

        return callOllama(prompt);
    }

    /**
     * Detect obvious update commands without depending on the LLM.
     *
     * Examples:
     *
     * Change the title of ticket 9 to Payment failed twice
     * Update ticket 9 description to money was deducted
     * Change ticket 9 priority to HIGH
     */
    
    private AiIntentResponse detectExplicitComment(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        String text = message.trim();

        Pattern pattern = Pattern.compile(
                "(?i)(?:add|write|post|leave)\\s+"
                + "(?:a\\s+)?comment\\s+"
                + "(?:to|on)\\s+"
                + "ticket\\s*#?(\\d+)\\s+"
                + "(?:saying|that|with)\\s+"
                + "(.+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        Long ticketId =
                Long.parseLong(matcher.group(1));

        String comment =
                matcher.group(2).trim();

        return AiIntentResponse.builder()
                .intent(AiIntent.ADD_COMMENT)
                .ticketId(ticketId)
                .comment(comment)
                .build();
    }
    
    
    private AiIntentResponse detectExplicitUpdate(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        String text = message.trim();

        Pattern pattern = Pattern.compile(
                "(?i)(?:change|update|modify|edit|rename)\\s+"
                + "(?:the\\s+)?"
                + "(title|description|priority)"
                + "\\s+"
                + "(?:of\\s+)?"
                + "ticket\\s*#?(\\d+)\\s+"
                + "to\\s+(.+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        String field =
                matcher.group(1).toUpperCase();

        Long ticketId =
                Long.parseLong(matcher.group(2));

        String value =
                matcher.group(3).trim();

        return AiIntentResponse.builder()
                .intent(com.supportai.entity.AiIntent.UPDATE_TICKET)
                .ticketId(ticketId)
                .updateField(field)
                .updateValue(value)
                .build();
    }

    private String buildPrompt(
            String message,
            String conversationContext) {

        return """
                You are an AI intent classifier for a customer support system.

                Determine what the user wants.

                Available intents:

                CREATE_TICKET
                CHECK_TICKET_STATUS
                LIST_MY_TICKETS
                ADD_COMMENT
                UPDATE_TICKET
                UNKNOWN

                IMPORTANT INTENT RULES:

                1. CREATE_TICKET

                Use CREATE_TICKET when the user wants to create/open a
                new support ticket.

                2. CHECK_TICKET_STATUS

                Use CHECK_TICKET_STATUS when the user asks about the
                status of an existing ticket.

                3. LIST_MY_TICKETS

                Use LIST_MY_TICKETS when the user asks to see/list
                their tickets.

                4. ADD_COMMENT

                ADD_COMMENT is ONLY when the user explicitly wants to
                add, write, post, or leave a comment/note.

                Examples:

                "Add a comment to ticket 8 saying payment failed"

                "Comment on ticket 8"

                "Add this note to ticket 8"

                "Tell the support team that payment failed"

                IMPORTANT:
                A request to change, update, modify, edit, or rename
                a ticket field is NOT ADD_COMMENT.

                5. UPDATE_TICKET

                If the user wants to change, update, modify, edit,
                or rename an existing ticket's title, description,
                or priority, ALWAYS classify it as UPDATE_TICKET.

                Examples:

                "Change the title of ticket 8 to Payment failed twice"

                "Update ticket 8 description to money was deducted
                but payment failed"

                "Change ticket 8 priority to HIGH"

                Allowed updateField values:

                TITLE
                DESCRIPTION
                PRIORITY

                For UPDATE_TICKET, return:

                {
                  "intent": "UPDATE_TICKET",
                  "ticketId": 8,
                  "updateField": "TITLE",
                  "updateValue": "Payment failed twice"
                }

                IMPORTANT:
                The field name MUST be "updateValue".

                DO NOT use:

                "newValue"
                "newTitle"
                "newDescription"
                "newPriority"

                6. TICKET ID RULE

                If the current user message explicitly contains a
                ticket number, ALWAYS use that ticket number.

                Never use a ticket ID from conversation history
                instead of an ID explicitly provided in the current
                message.

                7. UNKNOWN

                Return UNKNOWN if the requested operation is unclear.

                Conversation context:
                %s

                Current user message:
                %s

                Return ONLY valid JSON.

                JSON structure:

                {
                  "intent": "UPDATE_TICKET",
                  "ticketId": 8,
                  "title": null,
                  "description": null,
                  "comment": null,
                  "updateField": "TITLE",
                  "updateValue": "Payment failed twice"
                }
                """.formatted(
                        conversationContext,
                        message
                );
    }

    private AiIntentResponse callOllama(String prompt) {

        RestClient restClient =
                RestClient.builder()
                        .baseUrl(aiProperties.getBaseUrl())
                        .build();

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        Map<?, ?> response =
                restClient.post()
                        .uri("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

        if (response == null ||
                response.get("response") == null) {

            throw new RuntimeException(
                    "Invalid response from Ollama"
            );
        }

        String json =
                response.get("response")
                        .toString()
                        .trim();

        System.out.println(
                "========== AI RAW RESPONSE =========="
        );

        System.out.println(json);

        System.out.println(
                "====================================="
        );

        try {

            AiIntentResponse intentResponse =
                    objectMapper.readValue(
                            json,
                            AiIntentResponse.class
                    );

            System.out.println(
                    "========== PARSED AI INTENT =========="
            );

            System.out.println(
                    "Intent       : "
                    + intentResponse.getIntent()
            );

            System.out.println(
                    "Ticket ID    : "
                    + intentResponse.getTicketId()
            );

            System.out.println(
                    "Update Field : "
                    + intentResponse.getUpdateField()
            );

            System.out.println(
                    "Update Value : "
                    + intentResponse.getUpdateValue()
            );

            System.out.println(
                    "======================================="
            );

            return intentResponse;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to parse AI intent response: "
                    + json,
                    e
            );
        }
    }
}