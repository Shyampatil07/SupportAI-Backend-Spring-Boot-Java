package com.supportai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportai.dto.AiIntentResponse;
import com.supportai.entity.AiIntent;
import com.supportai.service.OllamaService;

@ExtendWith(MockitoExtension.class)
class AiIntentServiceImplTest {

    @Mock
    private OllamaService ollamaService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiIntentServiceImpl aiIntentService;
    
    @Test
    void shouldDetectCreateTicketIntent() throws Exception {

        String aiResponse = """
                {
                    "intent": "CREATE_TICKET",
                    "ticketId": null,
                    "title": "Payment failed",
                    "description": "My payment failed after money was deducted.",
                    "comment": null,
                    "updateField": null,
                    "updateValue": null
                }
                """;

        AiIntentResponse expected =
                AiIntentResponse.builder()
                        .intent(AiIntent.CREATE_TICKET)
                        .title("Payment failed")
                        .description(
                                "My payment failed after money was deducted."
                        )
                        .build();

        when(ollamaService.generate(anyString()))
                .thenReturn(aiResponse);

        when(objectMapper.readValue(
                anyString(),
                eq(AiIntentResponse.class)
        )).thenReturn(expected);

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Create a ticket because my payment failed.",
                        ""
                );

        assertNotNull(result);
        assertEquals(
                AiIntent.CREATE_TICKET,
                result.getIntent()
        );

        assertEquals(
                "Payment failed",
                result.getTitle()
        );

        verify(ollamaService)
                .generate(anyString());
    }
    
    @Test
    void shouldDetectCheckTicketStatusIntent() throws Exception {

        AiIntentResponse expected =
                AiIntentResponse.builder()
                        .intent(AiIntent.CHECK_TICKET_STATUS)
                        .ticketId(10L)
                        .build();

        when(ollamaService.generate(anyString()))
                .thenReturn("""
                        {
                            "intent": "CHECK_TICKET_STATUS",
                            "ticketId": 10
                        }
                        """);

        when(objectMapper.readValue(
                anyString(),
                eq(AiIntentResponse.class)
        )).thenReturn(expected);

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "What is the status of ticket 10?",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.CHECK_TICKET_STATUS,
                result.getIntent()
        );

        assertEquals(
                10L,
                result.getTicketId()
        );

        verify(ollamaService)
                .generate(anyString());
    }
    
    @Test
    void shouldDetectListMyTicketsIntent() throws Exception {

        AiIntentResponse expected =
                AiIntentResponse.builder()
                        .intent(AiIntent.LIST_MY_TICKETS)
                        .build();

        when(ollamaService.generate(anyString()))
                .thenReturn("""
                        {
                            "intent": "LIST_MY_TICKETS"
                        }
                        """);

        when(objectMapper.readValue(
                anyString(),
                eq(AiIntentResponse.class)
        )).thenReturn(expected);

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Show me all my tickets.",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.LIST_MY_TICKETS,
                result.getIntent()
        );

        verify(ollamaService)
                .generate(anyString());
    }
    
    @Test
    void shouldDetectAddCommentIntent() throws Exception {

        AiIntentResponse expected =
                AiIntentResponse.builder()
                        .intent(AiIntent.ADD_COMMENT)
                        .ticketId(10L)
                        .comment("The issue is still happening.")
                        .build();

        when(ollamaService.generate(anyString()))
                .thenReturn("""
                        {
                            "intent": "ADD_COMMENT",
                            "ticketId": 10,
                            "comment": "The issue is still happening."
                        }
                        """);

        when(objectMapper.readValue(
                anyString(),
                eq(AiIntentResponse.class)
        )).thenReturn(expected);

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Add a comment to ticket 10 saying the issue is still happening.",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.ADD_COMMENT,
                result.getIntent()
        );

        assertEquals(
                10L,
                result.getTicketId()
        );

        assertEquals(
                "The issue is still happening.",
                result.getComment()
        );

        verify(ollamaService)
                .generate(anyString());
    }
    
    
    @Test
    void shouldDetectExplicitUpdateWithoutCallingOllama() {

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Change the title of ticket 9 to Payment failed twice",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.UPDATE_TICKET,
                result.getIntent()
        );

        assertEquals(
                9L,
                result.getTicketId()
        );

        assertEquals(
                "TITLE",
                result.getUpdateField()
        );

        assertEquals(
                "Payment failed twice",
                result.getUpdateValue()
        );

        verifyNoInteractions(ollamaService);
    }
    
    
    
    @Test
    void shouldDetectPriorityUpdate() {

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Update priority of ticket 15 to HIGH",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.UPDATE_TICKET,
                result.getIntent()
        );

        assertEquals(
                15L,
                result.getTicketId()
        );

        assertEquals(
                "PRIORITY",
                result.getUpdateField()
        );

        assertEquals(
                "HIGH",
                result.getUpdateValue()
        );

        verifyNoInteractions(ollamaService);
    }
    
    @Test
    void shouldDetectUnknownIntent() throws Exception {

        AiIntentResponse expected =
                AiIntentResponse.builder()
                        .intent(AiIntent.UNKNOWN)
                        .build();

        when(ollamaService.generate(anyString()))
                .thenReturn("""
                        {
                            "intent": "UNKNOWN"
                        }
                        """);

        when(objectMapper.readValue(
                anyString(),
                eq(AiIntentResponse.class)
        )).thenReturn(expected);

        AiIntentResponse result =
                aiIntentService.detectIntent(
                        "Tell me something interesting.",
                        ""
                );

        assertNotNull(result);

        assertEquals(
                AiIntent.UNKNOWN,
                result.getIntent()
        );

        verify(ollamaService)
                .generate(anyString());
    }
    
    
    
    
}
