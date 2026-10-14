package com.supportai.service;

import com.supportai.dto.AiClassificationResponse;

public interface AiClassificationService {

    AiClassificationResponse classifyTicket(
            String title,
            String description);
}