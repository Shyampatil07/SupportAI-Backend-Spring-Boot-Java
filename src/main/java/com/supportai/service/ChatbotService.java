package com.supportai.service;

import com.supportai.dto.ChatResponse;

public interface ChatbotService {

	ChatResponse chat(
	        String userEmail,
	        Long conversationId,
	        String message
	);
}