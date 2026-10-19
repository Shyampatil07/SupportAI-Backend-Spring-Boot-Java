package com.supportai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportai.config.AiProperties;
import com.supportai.dto.AiIntentResponse;
import com.supportai.dto.ChatResponse;
import com.supportai.dto.TicketCommentRequest;
import com.supportai.dto.TicketCommentResponse;
import com.supportai.dto.TicketCreateRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.dto.TicketUpdateRequest;
import com.supportai.entity.AiIntent;
import com.supportai.entity.ChatConversation;
import com.supportai.entity.ChatMessage;
import com.supportai.entity.ChatMessageRole;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.User;
import com.supportai.repository.ChatConversationRepository;
import com.supportai.repository.ChatMessageRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.AiIntentService;
import com.supportai.service.ChatbotService;
import com.supportai.service.KnowledgeAnswerService;
import com.supportai.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    private final ChatConversationRepository
            conversationRepository;

    private final ChatMessageRepository
            messageRepository;

    private final AiProperties aiProperties;
    
    private final TicketCommentServiceImpl commentService;
    
    private final AiIntentService aiIntentService;
    private final KnowledgeAnswerService knowledgeAnswerService;

//    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public ChatResponse chat(
            String userEmail,
            Long conversationId,
            String message) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        ChatConversation conversation;

        if (conversationId == null) {

            conversation = ChatConversation.builder()
                    .user(user)
                    .build();

            conversation =
                    conversationRepository.save(conversation);

        } else {

            conversation =
                    conversationRepository
                            .findByIdAndUserEmail(
                                    conversationId,
                                    userEmail
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Conversation not found"
                                    ));
        }

        // Save user message
        ChatMessage userMessage =
                ChatMessage.builder()
                        .conversation(conversation)
                        .role(ChatMessageRole.USER)
                        .content(message)
                        .build();

        messageRepository.save(userMessage);

        // Get conversation history
        List<ChatMessage> messages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );
        

        // Build context
        String conversationContext =
                buildConversationContext(messages);

        // Detect intent
        AiIntentResponse intentResponse =
                aiIntentService.detectIntent(
                        message,
                        conversationContext
                );

        // Decide whether this is an action
        String actionResponse = handleIntent(
                intentResponse,
                userEmail,
                message,
                conversationContext
        );

        String aiResponse;

        if (actionResponse != null) {

            aiResponse = actionResponse;

        } else {

            // Existing V2 logic
            List<TicketResponse> tickets =
                    ticketService.getMyTickets(userEmail);

            String ticketContext =
                    buildTicketContext(tickets);

            String prompt =
                    buildPrompt(
                            message,
                            conversationContext,
                            ticketContext
                    );

            aiResponse =
                    callOllama(prompt);
        }

        // Save assistant response
        ChatMessage assistantMessage =
                ChatMessage.builder()
                        .conversation(conversation)
                        .role(ChatMessageRole.ASSISTANT)
                        .content(aiResponse)
                        .build();

        messageRepository.save(assistantMessage);

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .message(aiResponse)
                .build();
    }
    
    
    private String handleIntent(
            AiIntentResponse intentResponse,
            String userEmail,
            String message,
            String conversationContext) {

        if (intentResponse == null ||
                intentResponse.getIntent() == null) {

            return null;
        }

        switch (intentResponse.getIntent()) {

            case CREATE_TICKET:

                return createTicketFromAi(
                        intentResponse,
                        userEmail
                );
                
//            case CHECK_TICKET_STATUS:
//
//                return checkTicketStatus(
//                        intentResponse,
//                        userEmail
//                );
                
            case LIST_MY_TICKETS:

                return listMyTickets(userEmail);
                
            case UPDATE_TICKET:

                Long explicitTicketId = extractExplicitTicketId(message);

                if (explicitTicketId != null) {
                    intentResponse.setTicketId(explicitTicketId);
                }

                return updateTicketFromAi(intentResponse, userEmail);
                
//            case UPDATE_TICKET:
//                return updateTicketFromAi(
//                        intentResponse,
//                        userEmail
//                ); 
                
            case ADD_COMMENT:
            	
            	Long explicitCommentTicketId = extractExplicitTicketId(message);

                if (explicitCommentTicketId != null) {
                    intentResponse.setTicketId(explicitCommentTicketId);
                }

                return addCommentFromAi(
                        intentResponse,
                        userEmail
                );
               
            case CHECK_TICKET_STATUS:

                Long explicitStatusTicketId = extractExplicitTicketId(message);

                if (explicitStatusTicketId != null) {
                    intentResponse.setTicketId(explicitStatusTicketId);
                }

                return checkTicketStatus(intentResponse, userEmail);
                
            case UNKNOWN:
                return answerFromKnowledgeBase(message,
                		conversationContext );
                
           

            default:

                return null;
        }
    }
    
    private String answerFromKnowledgeBase(
            String message,
            String conversationContext) {

        return knowledgeAnswerService
                .answerQuestion(
                        message,
                        conversationContext
                );
    }
    
    
    
    
    
    private boolean isExplicitUpdateRequest(String message) {

        String text = message.toLowerCase().trim();

        return text.matches(
                ".*\\b(change|update|modify|edit|rename)\\b.*\\b(title|description|priority)\\b.*"
        );
    }
    
    private AiIntentResponse detectExplicitUpdate(String message) {

        String text = message.trim();

        Pattern pattern = Pattern.compile(
                "(?i)(?:change|update|modify|edit|rename)\\s+" +
                "(?:the\\s+)?" +
                "(title|description|priority)" +
                "\\s+(?:of\\s+)?" +
                "ticket\\s*#?(\\d+)\\s+" +
                "to\\s+(.+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        String field = matcher.group(1).toUpperCase();
        Long ticketId = Long.parseLong(matcher.group(2));
        String value = matcher.group(3).trim();

        return AiIntentResponse.builder()
                .intent(AiIntent.UPDATE_TICKET)
                .ticketId(ticketId)
                .updateField(field)
                .updateValue(value)
                .build();
    }
    
    private Long extractExplicitTicketId(String message) {

        Pattern pattern = Pattern.compile(
                "(?i)(?:ticket\\s*#?|#)\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return null;
    }
    
    private String updateTicketFromAi(
            AiIntentResponse intentResponse,
            String userEmail) {

        Long ticketId = intentResponse.getTicketId();

        String field = intentResponse.getUpdateField();

        String value = intentResponse.getUpdateValue();

        if (ticketId == null) {
            return "Please provide the ticket number so I can update it.";
        }

        if (field == null || field.isBlank()) {
            return "Please specify what you want to update.";
        }

        if (value == null || value.isBlank()) {
            return "Please provide the new value.";
        }

        TicketUpdateRequest request =
                new TicketUpdateRequest();

        switch (field.toUpperCase()) {

        case "TITLE":
            request.setTitle(value);
            break;

        case "DESCRIPTION":
            request.setDescription(value);
            break;

        case "PRIORITY":
            try {
                request.setPriority(
                    TicketPriority.valueOf(value.toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                return "Invalid priority. Please use LOW, MEDIUM, HIGH, or CRITICAL.";
            }
            break;

        default:
            return "I can currently update the title, description, or priority.";
    }

           

        ticketService.updateTicket(
                ticketId,
                request,
                userEmail
        );

        return String.format(
                "Ticket #%d has been updated successfully.",
                ticketId
        );
    }
    
    
    private String addCommentFromAi(
            AiIntentResponse intentResponse,
            String userEmail) {

        Long ticketId = intentResponse.getTicketId();

        String comment = intentResponse.getComment();

        if (ticketId == null) {
            return "Please provide the ticket number so I can add your comment.";
        }

        if (comment == null || comment.isBlank()) {
            return "Please provide the comment you want to add.";
        }

        TicketCommentRequest request =
                new TicketCommentRequest();

        request.setContent(comment);

        TicketCommentResponse savedComment =
                commentService.addComment(
                        ticketId,
                        request,
                        userEmail
                );

        return String.format(
                "Your comment has been added to ticket #%d.",
                savedComment.getTicketId()
        );
    }
    
    private String listMyTickets(String userEmail) {

        List<TicketResponse> tickets =
                ticketService.getMyTickets(userEmail);

        if (tickets.isEmpty()) {
            return "You currently don't have any tickets.";
        }

        StringBuilder response =
                new StringBuilder("Here are your tickets:\n\n");

        for (TicketResponse ticket : tickets) {

            response.append("Ticket #")
                    .append(ticket.getId())
                    .append(" - ")
                    .append(ticket.getTitle())
                    .append("\n");

            response.append("Status: ")
                    .append(ticket.getStatus())
                    .append("\n");

            response.append("Priority: ")
                    .append(ticket.getPriority())
                    .append("\n");

            response.append("Category: ")
                    .append(ticket.getCategory())
                    .append("\n\n");
        }

        return response.toString().trim();
    }
    
    private String checkTicketStatus(
            AiIntentResponse intentResponse,
            String userEmail) {

        Long ticketId = intentResponse.getTicketId();

        if (ticketId == null) {
            return "Please provide the ticket number so I can check its status.";
        }

        Optional<TicketResponse> ticketOptional =
                ticketService.getTicketForChat(
                        ticketId,
                        userEmail
                );

        if (ticketOptional.isEmpty()) {
            return "I couldn't find that ticket or you don't have access to it.";
        }

        TicketResponse ticket = ticketOptional.get();

        return String.format(
                "Ticket #%d is currently %s.",
                ticket.getId(),
                ticket.getStatus()
        );
    }
    
    private String createTicketFromAi(
            AiIntentResponse intentResponse,
            String userEmail) {

        TicketCreateRequest request =
                new TicketCreateRequest();

        request.setTitle(
                intentResponse.getTitle()
        );

        request.setDescription(
                intentResponse.getDescription()
        );

        TicketResponse ticket =
                ticketService.createTicket(
                        request,
                        userEmail
                );

        return String.format(
                "I've created ticket #%d for you. " +
                "It is currently %s with %s priority.",
                ticket.getId(),
                ticket.getStatus(),
                ticket.getPriority()
        );
    }


    private String buildTicketContext(
            List<TicketResponse> tickets) {

        if (tickets.isEmpty()) {
            return "The user has no tickets.";
        }

        StringBuilder context =
                new StringBuilder();

        for (TicketResponse ticket : tickets) {

            context.append("""
                    
                    Ticket ID: %d
                    Title: %s
                    Description: %s
                    Category: %s
                    Priority: %s
                    Status: %s
                    Assigned Agent: %s
                    
                    """.formatted(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getAssignedAgentName() != null
                            ? ticket.getAssignedAgentName()
                            : "Not assigned"
            ));
        }

        return context.toString();
    }


    private String buildConversationContext(
            List<ChatMessage> messages) {

//        StringBuilder context =
//                new StringBuilder();
//
//        for (ChatMessage message : messages) {
//
//            context.append(
//                    message.getRole()
//                            .name()
//            );
//
//            context.append(": ");
//
//            context.append(
//                    message.getContent()
//            );
//
//            context.append("\n");
//        }

        return messages.stream()
                .map(message ->
                        message.getRole()
                                + ": "
                                + message.getContent()
                )
                .collect(Collectors.joining("\n"));
    }


    private String buildPrompt(
            String currentMessage,
            String conversationContext,
            String ticketContext) {

        return """
                You are SupportAI, a helpful customer
                support assistant.

                You are talking to the authenticated user.

                IMPORTANT RULES:

                1. Use the conversation history to
                   understand follow-up questions.

                2. Use only the user's ticket information
                   provided below.

                3. Never invent ticket information.

                4. Never reveal another user's information.

                5. If the requested information is not
                   available, clearly say that you do not
                   have enough information.

                6. Be concise and helpful.

                7. If the user refers to a ticket number,
                   check whether that ticket exists in the
                   provided ticket context.

                CONVERSATION HISTORY:
                %s

                USER'S TICKETS:
                %s

                CURRENT USER MESSAGE:
                %s

                Respond naturally to the user.
                """.formatted(
                conversationContext,
                ticketContext,
                currentMessage
        );
    }


    private String callOllama(String prompt) {

        RestClient restClient =
                RestClient.builder()
                        .baseUrl(
                                aiProperties.getBaseUrl()
                        )
                        .build();

        Map<String, Object> requestBody =
                Map.of(
                        "model",
                        aiProperties.getModel(),

                        "prompt",
                        prompt,

                        "stream",
                        false
                );

        Map<?, ?> response =
                restClient.post()
                        .uri("/api/generate")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

        if (response == null ||
            response.get("response") == null) {

            throw new RuntimeException(
                    "Invalid response from Ollama"
            );
        }

        return response
                .get("response")
                .toString()
                .trim();
    }
}