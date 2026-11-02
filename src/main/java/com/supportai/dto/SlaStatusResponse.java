package com.supportai.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlaStatusResponse {

    private Long ticketId;
    private LocalDateTime slaDeadline;

    private boolean breached;

    private long remainingMinutes;

    private String status;
}