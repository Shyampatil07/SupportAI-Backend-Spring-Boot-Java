package com.supportai.service;

import java.time.LocalDateTime;

import com.supportai.dto.SlaStatusResponse;
import com.supportai.entity.TicketPriority;

public interface SlaService {

    LocalDateTime calculateDeadline(
            LocalDateTime createdAt,
            TicketPriority priority);
    
    SlaStatusResponse getSlaStatus(
            Long ticketId,
            String userEmail);
}