package com.supportai.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.TicketHistoryResponse;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.User;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.TicketHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketHistoryServiceImpl
        implements TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getTicketHistory(
            Long ticketId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail) 
        		.orElseThrow(() -> new ResourceNotFoundException("User not found"));
        		

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        boolean isOwner =
                ticket.getCreatedBy().getId()
                        .equals(user.getId());

        boolean isAssignedAgent =
                ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getId()
                        .equals(user.getId());

        boolean isAdmin =
                user.getRole().name().equals("ADMIN");

        if (!isOwner && !isAssignedAgent && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not allowed to view this ticket history");
        }

        return ticketHistoryRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TicketHistoryResponse mapToResponse(
            TicketHistory history) {

        return TicketHistoryResponse.builder()
                .id(history.getId())
                .ticketId(history.getTicket().getId())
                .changedById(history.getChangedBy().getId())
                .changedByName(history.getChangedBy().getName())
                .action(history.getAction())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .createdAt(history.getCreatedAt())
                .build();
    }
}