package com.supportai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supportai.dto.TicketHistoryResponse;
import com.supportai.service.TicketHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    @GetMapping("/{ticketId}/history")
    public ResponseEntity<List<TicketHistoryResponse>> getTicketHistory(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<TicketHistoryResponse> history =
                ticketHistoryService.getTicketHistory(
                        ticketId,
                        userEmail);

        return ResponseEntity.ok(history);
    }
}