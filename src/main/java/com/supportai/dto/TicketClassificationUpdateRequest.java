package com.supportai.dto;

import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketClassificationUpdateRequest {

    @NotNull(message = "Category is required")
    private TicketCategory category;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}