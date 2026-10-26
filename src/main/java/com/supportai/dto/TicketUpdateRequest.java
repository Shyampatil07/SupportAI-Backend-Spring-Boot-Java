package com.supportai.dto;

import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketUpdateRequest {

    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    private String description;

    private TicketCategory category;

    private TicketPriority priority;
}