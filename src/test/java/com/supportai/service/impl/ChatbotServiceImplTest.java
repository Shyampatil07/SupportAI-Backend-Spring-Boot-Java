package com.supportai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.supportai.dto.AiIntentResponse;
import com.supportai.dto.ChatResponse;
import com.supportai.dto.TicketResponse;
import com.supportai.entity.AiIntent;
//import com.supportai.enums.AiIntent;
import com.supportai.service.AiIntentService;
import com.supportai.service.KnowledgeAnswerService;
import com.supportai.service.TicketCommentService;
import com.supportai.service.TicketService;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceImplTest {

    @Mock
    private AiIntentService aiIntentService;

    @Mock
    private KnowledgeAnswerService knowledgeAnswerService;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketCommentService ticketCommentService;

    @InjectMocks
    private ChatbotServiceImpl chatbotService;
    
    
    @Test
    void shouldAnswerKnowledgeQuestion() {

        when(aiIntentService.detectIntent(
                anyString(),
                anyString()
        )).thenReturn(
                AiIntentResponse.builder()
                        .intent(AiIntent.UNKNOWN)
                        .build()
        );

        when(knowledgeAnswerService.answerQuestion(
                eq("How can I reset my password?"),
                anyString()
        )).thenReturn(
                "Click Forgot Password and follow the instructions sent to your email."
        );

        ChatResponse response =
                chatbotService.chat(
                        "user@example.com",
                        null,
                        "How can I reset my password?"
                );

        assertNotNull(response);

        assertEquals(
                "Click Forgot Password and follow the instructions sent to your email.",
                response.getMessage()
        );

        verify(knowledgeAnswerService)
                .answerQuestion(
                        eq("How can I reset my password?"),
                        anyString()
                );
    }
    
    @Test
    void shouldCreateTicketFromChat() {

        when(aiIntentService.detectIntent(
                anyString(),
                anyString()
        )).thenReturn(
                AiIntentResponse.builder()
                        .intent(AiIntent.CREATE_TICKET)
                        .title("Payment failed")
                        .description(
                                "Money was deducted but payment failed."
                        )
                        .build()
        );

        TicketResponse ticket =
                TicketResponse.builder()
                        .id(25L)
                        .title("Payment failed")
                        .description(
                                "Money was deducted but payment failed."
                        )
                        .build();

        when(ticketService.createTicket(
                any(),
                eq("user@example.com")
        )).thenReturn(ticket);

        ChatResponse response =
                chatbotService.chat(
                        "user@example.com",
                        null,
                        "Create a ticket for my failed payment."
                );

        assertNotNull(response);

        assertTrue(
                response.getMessage()
                        .contains("25")
        );

        verify(ticketService)
                .createTicket(
                        any(),
                        eq("user@example.com")
                );

        verifyNoInteractions(knowledgeAnswerService);
    }
    
    @Test
    void shouldListMyTickets() {

        when(aiIntentService.detectIntent(
                anyString(),
                anyString()
        )).thenReturn(
                AiIntentResponse.builder()
                        .intent(AiIntent.LIST_MY_TICKETS)
                        .build()
        );

        TicketResponse ticket1 =
                TicketResponse.builder()
                        .id(10L)
                        .title("Payment failed")
                        .status(null)
                        .build();

        TicketResponse ticket2 =
                TicketResponse.builder()
                        .id(11L)
                        .title("Password reset")
                        .status(null)
                        .build();

        when(ticketService.getMyTickets(
                "user@example.com"
        )).thenReturn(
                List.of(ticket1, ticket2)
        );

        ChatResponse response =
                chatbotService.chat(
                        "user@example.com",
                        null,
                        "Show me my tickets."
                );

        assertNotNull(response);

        assertTrue(
                response.getMessage()
                        .contains("10")
        );

        assertTrue(
                response.getMessage()
                        .contains("11")
        );

        verify(ticketService)
                .getMyTickets("user@example.com");

        verifyNoInteractions(knowledgeAnswerService);
    }
    
    @Test
    void shouldAddCommentFromChat() {

        when(aiIntentService.detectIntent(
                anyString(),
                anyString()
        )).thenReturn(
                AiIntentResponse.builder()
                        .intent(AiIntent.ADD_COMMENT)
                        .ticketId(10L)
                        .comment(
                                "The issue is still happening."
                        )
                        .build()
        );

        ChatResponse response =
                chatbotService.chat(
                        "user@example.com",
                        null,
                        "Add a comment to ticket 10."
                );

        assertNotNull(response);

        verify(ticketCommentService)
                .addComment(
                        eq(10L),
                        any(),
                        eq("user@example.com")
                );
    }
    
    @Test
    void shouldUpdateTicketFromChat() {

        when(aiIntentService.detectIntent(
                anyString(),
                anyString()
        )).thenReturn(
                AiIntentResponse.builder()
                        .intent(AiIntent.UPDATE_TICKET)
                        .ticketId(10L)
                        .updateField("TITLE")
                        .updateValue("Payment failed twice")
                        .build()
        );

        TicketResponse updatedTicket =
                TicketResponse.builder()
                        .id(10L)
                        .title("Payment failed twice")
                        .build();

        when(ticketService.updateTicket(
                eq(10L),
                any(),
                eq("user@example.com")
        )).thenReturn(updatedTicket);

        ChatResponse response =
                chatbotService.chat(
                        "user@example.com",
                        null,
                        "Change the title of ticket 10 to Payment failed twice."
                );

        assertNotNull(response);

        assertTrue(
                response.getMessage()
                        .contains("10")
        );

        verify(ticketService)
                .updateTicket(
                        eq(10L),
                        any(),
                        eq("user@example.com")
                );
    }
    
    
    
    

}
