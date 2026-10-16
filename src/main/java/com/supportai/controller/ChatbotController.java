package com.supportai.controller;

import com.supportai.dto.ChatRequest;
import com.supportai.dto.ChatResponse;
import com.supportai.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {

    	return chatbotService.chat(
    	        authentication.getName(),
    	        request.getConversationId(),
    	        request.getMessage()
    	);
    }
}