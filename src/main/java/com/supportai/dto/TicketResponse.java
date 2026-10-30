package com.supportai.dto;

import java.time.LocalDateTime;

import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketResponse {

    private Long id;

    private String title;

    private String description;

    private TicketCategory category;

    private TicketPriority priority;
    
    private TicketCategory aiCategory;
    private TicketPriority aiPriority;
    private String aiReason;

    private TicketStatus status;

    private Long createdById;

    private String createdByName;

    private Long assignedAgentId;

    private String assignedAgentName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}