package com.supportai.service;

import com.supportai.dto.AiIntentResponse;

public interface AiIntentService {

    AiIntentResponse detectIntent(
            String message,
            String conversationContext
    );
}