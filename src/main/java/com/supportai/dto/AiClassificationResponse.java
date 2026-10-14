package com.supportai.dto;

import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiClassificationResponse {

    private TicketCategory category;

    private TicketPriority priority;

    private String reason;
}