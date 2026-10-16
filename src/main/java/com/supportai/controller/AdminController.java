package com.supportai.controller;

import com.supportai.dto.RoleUpdateRequest;
import com.supportai.dto.TicketAssignmentRequest;
import com.supportai.dto.TicketClassificationUpdateRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.dto.UserResponse;
import com.supportai.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supportai.service.TicketService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final TicketService ticketService;
    
    

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        UserResponse response =
                userService.updateUserRole(
                        id,
                        request.getRole());

        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketAssignmentRequest request) {

        TicketResponse response =
                ticketService.assignTicket(
                        ticketId,
                        request.getAgentId(),
                        request.getAssignedByEmail()
                        );

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/tickets/{ticketId}/classification")
    @PreAuthorize("hasRole('ADMIN')")
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