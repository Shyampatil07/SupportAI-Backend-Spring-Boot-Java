package com.supportai.dto;

import com.supportai.entity.TicketPriority;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketPriorityUpdateRequest {

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}