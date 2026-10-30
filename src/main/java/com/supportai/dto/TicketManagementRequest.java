package com.supportai.dto;

import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketManagementRequest {

    private TicketStatus status;

    private TicketPriority priority;

    private Long assignedAgentId;
}