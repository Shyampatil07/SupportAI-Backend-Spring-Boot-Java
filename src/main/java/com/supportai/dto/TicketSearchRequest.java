package com.supportai.dto;

import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketSearchRequest {

    private TicketStatus status;

    private TicketPriority priority;

    private TicketCategory category;

    private String search;
}