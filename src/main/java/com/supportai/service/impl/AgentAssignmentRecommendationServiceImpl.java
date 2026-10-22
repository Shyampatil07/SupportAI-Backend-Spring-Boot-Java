package com.supportai.service.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.AgentRecommendationResponse;
import com.supportai.dto.TicketResponse;
import com.supportai.entity.Role;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import com.supportai.entity.User;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.AgentAssignmentRecommendationService;
import com.supportai.service.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentAssignmentRecommendationServiceImpl
        implements AgentAssignmentRecommendationService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    
    private final TicketService ticketService;

    @Override
    @Transactional(readOnly = true)
    public List<AgentRecommendationResponse> recommendAgent(
            Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));

        List<User> agents =
                userRepository.findByRole(Role.AGENT);

        if (agents.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No agents available for assignment");
        }

        return agents.stream()
                .map(agent -> calculateScore(agent, ticket))
                .sorted(Comparator.comparingDouble(
                        AgentRecommendationResponse::getTotalScore))
                .limit(3)
                .toList();
    }

    private AgentRecommendationResponse calculateScore(
            User agent,
            Ticket ticket) {

        long openTickets =
                ticketRepository
                        .countByAssignedAgentIdAndStatus(
                                agent.getId(),
                                TicketStatus.OPEN);

        long inProgressTickets =
                ticketRepository
                        .countByAssignedAgentIdAndStatus(
                                agent.getId(),
                                TicketStatus.IN_PROGRESS);

        long activeTickets =
                openTickets + inProgressTickets;

        /*
         * Priority multiplier.
         *
         * Higher priority means workload matters more.
         */
        double priorityScore =
                switch (ticket.getPriority()) {

                    case CRITICAL -> 2.0;
                    case HIGH -> 1.5;
                    case MEDIUM -> 1.0;
                    case LOW -> 0.5;
                };

        /*
         * Historical category expertise.
         *
         * We consider previously RESOLVED tickets
         * in the same category.
         */
        long categoryResolvedTickets =
                ticketRepository
                        .countByAssignedAgentIdAndCategoryAndStatus(
                                agent.getId(),
                                ticket.getCategory(),
                                TicketStatus.RESOLVED);

        /*
         * More resolved tickets in this category
         * means stronger expertise.
         */
        double expertiseScore =
                categoryResolvedTickets * 0.5;

        /*
         * Lower total score = better recommendation.
         *
         * Workload increases score.
         * Priority increases workload impact.
         * Expertise reduces score.
         */
        double workloadScore =
                activeTickets * priorityScore;

        double totalScore =
                workloadScore - expertiseScore;

        String reason =
                "Active workload: " + activeTickets
                + ", resolved "
                + categoryResolvedTickets
                + " tickets in "
                + ticket.getCategory()
                + " category.";

        return new AgentRecommendationResponse(
                agent.getId(),
                agent.getName(),
                activeTickets,
                openTickets,
                inProgressTickets,
                categoryResolvedTickets,
                workloadScore,
                priorityScore,
                expertiseScore,
                totalScore,
                reason
        );
    }
    
    @Override
    @Transactional
    public TicketResponse autoAssignTicket(
            Long ticketId,
            String adminEmail) {

        List<AgentRecommendationResponse> recommendations =
                recommendAgent(ticketId);

        if (recommendations.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No suitable agent found for assignment");
        }

        AgentRecommendationResponse bestAgent =
                recommendations.get(0);

        return ticketService.assignTicket(
                ticketId,
                bestAgent.getAgentId(),
                adminEmail
        );
    }
    
    
    
}