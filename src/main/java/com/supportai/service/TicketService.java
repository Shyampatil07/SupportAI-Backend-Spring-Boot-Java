package com.supportai.service;

import com.supportai.dto.TicketClassificationUpdateRequest;
import com.supportai.dto.TicketCreateRequest;
import com.supportai.dto.TicketManagementRequest;

import java.util.List;
import java.util.Optional;

import com.supportai.dto.TicketResponse;
import com.supportai.dto.TicketUpdateRequest;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketResponse createTicket(
            TicketCreateRequest request,
            String userEmail
    );
    
    List<TicketResponse> getMyTickets(String userEmail);
    
    TicketResponse getTicketById(
            Long ticketId,
            String userEmail
    );
    
    TicketResponse updateTicket(
            Long ticketId,
            TicketUpdateRequest request,
            String userEmail
    );
    
    TicketResponse manageTicket(
            Long ticketId,
            TicketManagementRequest request,
            String userEmail
    );
    
    TicketResponse assignTicket(
            Long ticketId,
            Long agentId,
            String userEmail);
    
    List<TicketResponse> getAssignedTickets(String userEmail);
    
    TicketResponse updateTicketStatus(
            Long ticketId,
            TicketStatus status,
            String userEmail);

    TicketResponse updateTicketPriority(
            Long ticketId,
            TicketPriority priority,
            String userEmail);
    
    TicketResponse updateTicketClassification(
            Long ticketId,
            TicketClassificationUpdateRequest request,
            String userEmail
    );
    
    Optional<TicketResponse> getTicketForChat(
            Long ticketId,
            String userEmail
    );
    
    Page<TicketResponse> searchAssignedTickets(
            String userEmail,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search,
            Pageable pageable
    );
    
    Page<TicketResponse> searchMyTickets(
            String userEmail,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String search,
            Pageable pageable
    );
    
}