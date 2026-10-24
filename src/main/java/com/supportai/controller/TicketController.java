package com.supportai.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supportai.dto.SlaStatusResponse;
import com.supportai.dto.TicketCreateRequest;
import com.supportai.dto.TicketManagementRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.dto.TicketUpdateRequest;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import com.supportai.service.SlaService;
import com.supportai.service.TicketService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    
    private final SlaService slaService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.createTicket(
                        request,
                        userEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @GetMapping("/my/search")
    public Page<TicketResponse> searchMyTickets(

            @RequestParam(required = false)
            TicketStatus status,

            @RequestParam(required = false)
            TicketPriority priority,

            @RequestParam(required = false)
            TicketCategory category,

            @RequestParam(required = false)
            String search,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            Authentication authentication) {

        return ticketService.searchMyTickets(
                authentication.getName(),
                status,
                priority,
                category,
                search,
                pageable
        );
    }
    
    
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getMyTickets(userEmail);

        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.getTicketById(
                        id,
                        userEmail
                );

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.updateTicket(
                        id,
                        request,
                        userEmail);

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/manage")
    public ResponseEntity<TicketResponse> manageTicket(
            @PathVariable Long id,
            @RequestBody TicketManagementRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketResponse response =
                ticketService.manageTicket(
                        id,
                        request,
                        userEmail);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{ticketId}/sla")
    public SlaStatusResponse getSlaStatus(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return slaService.getSlaStatus(
                ticketId,
                authentication.getName()
        );
    }
    
    
}