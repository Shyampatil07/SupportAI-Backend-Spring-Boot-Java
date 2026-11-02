package com.supportai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketAssignmentRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;
    
    @NotNull(message = "Assigned by email is required")
    private String assignedByEmail;
    
    
}