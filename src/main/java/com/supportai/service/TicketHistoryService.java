package com.supportai.service;

import java.util.List;

import com.supportai.dto.TicketHistoryResponse;

public interface TicketHistoryService {

    List<TicketHistoryResponse> getTicketHistory(
            Long ticketId,
            String userEmail);
}