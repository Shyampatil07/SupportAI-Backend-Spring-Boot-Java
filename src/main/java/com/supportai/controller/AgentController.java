package com.supportai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supportai.dto.TicketClassificationUpdateRequest;
import com.supportai.dto.TicketPriorityUpdateRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.dto.TicketStatusUpdateRequest;
import com.supportai.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final TicketService ticketService;

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/tickets/assigned")
    public ResponseEntity<List<TicketResponse>> getAssignedTickets(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getAssignedTickets(userEmail);

        return ResponseEntity.ok(tickets);
    }
    
    @PreAuthorize("hasRole('AGENT')")
    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.updateTicketStatus(
                        id,
                        request.getStatus(),
                        userEmail);

        return ResponseEntity.ok(response);
    }
    
    
    @PreAuthorize("hasRole('AGENT')")
    @PatchMapping("/tickets/{id}/priority")
    public ResponseEntity<TicketResponse> updateTicketPriority(
            @PathVariable Long id,
            @Valid @RequestBody TicketPriorityUpdateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.updateTicketPriority(
                        id,
                        request.getPriority(),
                        userEmail);

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/tickets/{ticketId}/classification")
    @PreAuthorize("hasRole('AGENT')")
    public TicketResponse updateTicketClassification(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketClassificationUpdateRequest request,
            Authentication authentication) {

        return ticketService.updateTicketClassification(
                ticketId,
                request,
                authentication.getName()
        );
    }
    
    
}