package com.supportai.dto;

import java.time.LocalDateTime;

import com.supportai.entity.TicketHistoryAction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketHistoryResponse {

    private Long id;

    private Long ticketId;

    private Long changedById;

    private String changedByName;

    private TicketHistoryAction action;

    private String oldValue;

    private String newValue;

    private LocalDateTime createdAt;
}