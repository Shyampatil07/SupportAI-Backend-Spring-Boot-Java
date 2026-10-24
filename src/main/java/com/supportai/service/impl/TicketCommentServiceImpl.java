package com.supportai.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.TicketCommentRequest;
import com.supportai.dto.TicketCommentResponse;
import com.supportai.entity.Role;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketComment;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;
import com.supportai.entity.User;
import com.supportai.enums.NotificationType;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.TicketCommentRepository;
import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.NotificationService;
import com.supportai.service.TicketCommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketCommentServiceImpl
        implements TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public TicketCommentResponse addComment(
            Long ticketId,
            TicketCommentRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        checkAccess(ticket, user);

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(user)
                .content(request.getContent().trim())
                .build();

        TicketComment savedComment =
                ticketCommentRepository.save(comment);


        // Save ticket history
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(user)
                .action(TicketHistoryAction.COMMENT_ADDED)
                .oldValue(null)
                .newValue(savedComment.getContent())
                .build();

        ticketHistoryRepository.save(history);
        
        if (ticket.getCreatedBy().getId().equals(user.getId())) {

            // Customer commented → notify assigned agent
            if (ticket.getAssignedAgent() != null) {

                notificationService.createNotification(
                        ticket.getAssignedAgent().getId(),
                        "New Comment Added",
                        "A new comment was added to Ticket #"
                                + ticket.getId()
                                + ".",
                        NotificationType.TICKET_COMMENT_ADDED
                );
            }

        } else {

            // Agent/Admin commented → notify customer
            notificationService.createNotification(
                    ticket.getCreatedBy().getId(),
                    "New Comment Added",
                    "A new comment was added to Ticket #"
                            + ticket.getId()
                            + ".",
                    NotificationType.TICKET_COMMENT_ADDED
            );
        }


        return mapToResponse(savedComment);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getComments(
            Long ticketId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: "
                                        + ticketId));

        checkAccess(ticket, user);

        return ticketCommentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    private void checkAccess(
            Ticket ticket,
            User user) {

        boolean isOwner =
                ticket.getCreatedBy()
                        .getId()
                        .equals(user.getId());

        boolean isAssignedAgent =
                ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent()
                        .getId()
                        .equals(user.getId());

        boolean isAdmin =
                user.getRole() == Role.ADMIN;

        if (!isOwner && !isAssignedAgent && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not allowed to access comments for this ticket");
        }
    }


    private TicketCommentResponse mapToResponse(
            TicketComment comment) {

        return TicketCommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getName())
                .userRole(comment.getUser().getRole().name())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}