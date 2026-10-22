package com.supportai.service.impl;

import com.supportai.dto.AgentDashboardResponse;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import com.supportai.entity.User;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.NotificationRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.AgentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentDashboardServiceImpl
        implements AgentDashboardService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public AgentDashboardResponse getDashboard(
            String userEmail) {

        User agent = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        long totalAssigned =
                ticketRepository.countByAssignedAgentId(
                        agent.getId()
                );

        long openTickets =
                ticketRepository
                        .countByAssignedAgentIdAndStatus(
                                agent.getId(),
                                TicketStatus.OPEN
                        );

        long inProgressTickets =
                ticketRepository
                        .countByAssignedAgentIdAndStatus(
                                agent.getId(),
                                TicketStatus.IN_PROGRESS
                        );

        long resolvedTickets =
                ticketRepository
                        .countByAssignedAgentIdAndStatus(
                                agent.getId(),
                                TicketStatus.RESOLVED
                        );

        long highPriorityTickets =
                ticketRepository
                        .countByAssignedAgentIdAndPriority(
                                agent.getId(),
                                TicketPriority.HIGH
                        );

        long unreadNotifications =
                notificationRepository
                        .countByUserIdAndIsReadFalse(
                                agent.getId()
                        );

        return AgentDashboardResponse.builder()
                .totalAssigned(totalAssigned)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .highPriorityTickets(highPriorityTickets)
                .unreadNotifications(unreadNotifications)
                .build();
    }
}