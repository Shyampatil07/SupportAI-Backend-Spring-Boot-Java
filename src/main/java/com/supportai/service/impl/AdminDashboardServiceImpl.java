package com.supportai.service.impl;

import com.supportai.dto.AdminDashboardResponse;
import com.supportai.dto.AgentWorkloadResponse;
import com.supportai.dto.OverloadedAgentResponse;
import com.supportai.entity.Role;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;
import com.supportai.entity.User;
import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import com.supportai.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.supportai.dto.DateRangeAnalyticsResponse;
import com.supportai.dto.HistoricalAnalyticsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl
        implements AdminDashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private static final long OVERLOAD_THRESHOLD = 10;
    private final TicketHistoryRepository ticketHistoryRepository;
    
    private double calculateAverageResolutionTime(
            List<TicketHistory> resolvedHistory) {

        if (resolvedHistory.isEmpty()) {
            return 0.0;
        }

        double totalHours = 0.0;
        int resolutionCycles = 0;

        for (TicketHistory resolved : resolvedHistory) {

            Ticket ticket = resolved.getTicket();

            LocalDateTime resolutionStart = ticket.getCreatedAt();

            // Find the most recent reopen before this resolution
            TicketHistory latestReopen = null;

            List<TicketHistory> historyList =
                    ticketHistoryRepository
                            .findByTicketIdOrderByCreatedAtAsc(ticket.getId());

            for (TicketHistory history : historyList) {

                if (history.getCreatedAt().isAfter(
                        resolved.getCreatedAt())) {
                    break;
                }

                boolean reopened =
                        TicketHistoryAction.STATUS_CHANGED
                                .equals(history.getAction())
                        && (
                            TicketStatus.OPEN.name()
                                    .equals(history.getNewValue())
                            ||
                            TicketStatus.IN_PROGRESS.name()
                                    .equals(history.getNewValue())
                        )
                        && (
                            TicketStatus.RESOLVED.name()
                                    .equals(history.getOldValue())
                            ||
                            TicketStatus.CLOSED.name()
                                    .equals(history.getOldValue())
                        );

                if (reopened) {
                    latestReopen = history;
                }
            }

            if (latestReopen != null) {
                resolutionStart = latestReopen.getCreatedAt();
            }

            long minutes =
                    Duration.between(
                            resolutionStart,
                            resolved.getCreatedAt()
                    ).toMinutes();

            totalHours += minutes / 60.0;
            resolutionCycles++;
        }

        return Math.round(
                (totalHours / resolutionCycles) * 100.0
        ) / 100.0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        long totalTickets = ticketRepository.count();

        long openTickets =
                ticketRepository.countByStatus(TicketStatus.OPEN);

        long inProgressTickets =
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);

        long resolvedTickets =
                ticketRepository.countByStatus(TicketStatus.RESOLVED);

        long closedTickets =
                ticketRepository.countByStatus(TicketStatus.CLOSED);


        Map<String, Long> statusDistribution =
                new LinkedHashMap<>();

        for (TicketStatus status : TicketStatus.values()) {
            statusDistribution.put(
                    status.name(),
                    ticketRepository.countByStatus(status)
            );
        }


        Map<String, Long> priorityDistribution =
                new LinkedHashMap<>();

        for (TicketPriority priority : TicketPriority.values()) {
            priorityDistribution.put(
                    priority.name(),
                    ticketRepository.countByPriority(priority)
            );
        }


        Map<String, Long> categoryDistribution =
                new LinkedHashMap<>();

        for (TicketCategory category : TicketCategory.values()) {
            categoryDistribution.put(
                    category.name(),
                    ticketRepository.countByCategory(category)
            );
        }
        
        long unassignedTickets =
                ticketRepository.countByAssignedAgentIsNull();


        List<User> agents =
                userRepository.findByRole(Role.AGENT);

        List<AgentWorkloadResponse> agentWorkload =
                agents.stream()
                        .map(agent -> {

                            long assignedTickets =
                                    ticketRepository
                                            .countByAssignedAgentId(
                                                    agent.getId()
                                            );

                            long openAgentTickets =
                                    ticketRepository
                                            .countByAssignedAgentIdAndStatus(
                                                    agent.getId(),
                                                    TicketStatus.OPEN
                                            );

                            long inProgressAgentTickets =
                                    ticketRepository
                                            .countByAssignedAgentIdAndStatus(
                                                    agent.getId(),
                                                    TicketStatus.IN_PROGRESS
                                            );

                            long resolvedAgentTickets =
                                    ticketRepository
                                            .countByAssignedAgentIdAndStatus(
                                                    agent.getId(),
                                                    TicketStatus.RESOLVED
                                            );

                            long closedAgentTickets =
                                    ticketRepository
                                            .countByAssignedAgentIdAndStatus(
                                                    agent.getId(),
                                                    TicketStatus.CLOSED
                                            );

                            return new AgentWorkloadResponse(
                                    agent.getId(),
                                    agent.getName(),
                                    assignedTickets,
                                    openAgentTickets,
                                    inProgressAgentTickets,
                                    resolvedAgentTickets,
                                    closedAgentTickets
                            );
                        })
                        .toList();
        
        List<OverloadedAgentResponse> overloadedAgents =
                agents.stream()
                        .map(agent -> {

                        	long overloadedOpenTickets =
                        	        ticketRepository
                        	                .countByAssignedAgentIdAndStatus(
                        	                        agent.getId(),
                        	                        TicketStatus.OPEN
                        	                );

                        	long overloadedInProgressTickets =
                        	        ticketRepository
                        	                .countByAssignedAgentIdAndStatus(
                        	                        agent.getId(),
                        	                        TicketStatus.IN_PROGRESS
                        	                );

                        	long activeTickets =
                        	        overloadedOpenTickets + overloadedInProgressTickets;

                        	return new OverloadedAgentResponse(
                        	        agent.getId(),
                        	        agent.getName(),
                        	        activeTickets,
                        	        overloadedOpenTickets,
                        	        overloadedInProgressTickets
                        	);
                        })
                        .filter(agent ->
                                agent.getActiveTickets() >= OVERLOAD_THRESHOLD
                        )
                        .toList();
        

        return new AdminDashboardResponse(
                totalTickets,
                openTickets,
                inProgressTickets,
                resolvedTickets,
                closedTickets,
                statusDistribution,
                priorityDistribution,
                categoryDistribution,
                agentWorkload,
                unassignedTickets,
                overloadedAgents
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public DateRangeAnalyticsResponse getDateRangeAnalytics(
            LocalDate from,
            LocalDate to) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' date cannot be after 'to' date"
            );
        }

        LocalDateTime startDate =
                from.atStartOfDay();

        LocalDateTime endDate =
                to.plusDays(1).atStartOfDay();


        long ticketsCreated =
                ticketRepository
                        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDate,
                                endDate
                        );


        Map<String, Long> statusDistribution =
                new LinkedHashMap<>();

        for (TicketStatus status : TicketStatus.values()) {

            long count =
                    ticketRepository
                            .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStatus(
                                    startDate,
                                    endDate,
                                    status
                            );

            statusDistribution.put(
                    status.name(),
                    count
            );
        }


        Map<String, Long> priorityDistribution =
                new LinkedHashMap<>();

        for (TicketPriority priority : TicketPriority.values()) {

            long count =
                    ticketRepository
                            .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndPriority(
                                    startDate,
                                    endDate,
                                    priority
                            );

            priorityDistribution.put(
                    priority.name(),
                    count
            );
        }


        Map<String, Long> categoryDistribution =
                new LinkedHashMap<>();

        for (TicketCategory category : TicketCategory.values()) {

            long count =
                    ticketRepository
                            .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndCategory(
                                    startDate,
                                    endDate,
                                    category
                            );

            categoryDistribution.put(
                    category.name(),
                    count
            );
        }


        return new DateRangeAnalyticsResponse(
                from,
                to,
                ticketsCreated,
                statusDistribution,
                priorityDistribution,
                categoryDistribution
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public HistoricalAnalyticsResponse getHistoricalAnalytics(
            LocalDate from,
            LocalDate to) {
    	
    	List<TicketHistory> statusHistory =
    	        ticketHistoryRepository
    	                .findByActionOrderByCreatedAtAsc(
    	                        TicketHistoryAction.STATUS_CHANGED
    	                );

    	System.out.println("===== STATUS HISTORY =====");

    	for (TicketHistory history : statusHistory) {

    	    System.out.println(
    	            "Ticket ID: " + history.getTicket().getId()
    	            + " | Old: " + history.getOldValue()
    	            + " | New: " + history.getNewValue()
    	            + " | Date: " + history.getCreatedAt()
    	    );
    	}

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' date cannot be after 'to' date"
            );
        }

        LocalDateTime startDate =
                from.atStartOfDay();

        LocalDateTime endDate =
                to.plusDays(1).atStartOfDay();


        List<TicketHistory> resolvedHistory =
                ticketHistoryRepository
                        .findByActionAndNewValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                TicketHistoryAction.STATUS_CHANGED,
                                TicketStatus.RESOLVED.name(),
                                startDate,
                                endDate
                        );


        List<TicketHistory> closedHistory =
                ticketHistoryRepository
                        .findByActionAndNewValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                TicketHistoryAction.STATUS_CHANGED,
                                TicketStatus.CLOSED.name(),
                                startDate,
                                endDate
                        );


        List<TicketHistory> resolvedTransitions =
                ticketHistoryRepository
                        .findByActionAndOldValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                TicketHistoryAction.STATUS_CHANGED,
                                TicketStatus.RESOLVED.name(),
                                startDate,
                                endDate
                        );

        List<TicketHistory> closedTransitions =
                ticketHistoryRepository
                        .findByActionAndOldValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                TicketHistoryAction.STATUS_CHANGED,
                                TicketStatus.CLOSED.name(),
                                startDate,
                                endDate
                        );

        long ticketsReopened =
                Stream.concat(
                        resolvedTransitions.stream(),
                        closedTransitions.stream()
                )
                .filter(history ->
                        TicketStatus.OPEN.name().equals(history.getNewValue())
                        || TicketStatus.IN_PROGRESS.name().equals(history.getNewValue())
                )
                .count();

//        long ticketsReopened = 0;

        long ticketsResolved =
                resolvedHistory.size();

        long ticketsClosed =
                closedHistory.size();

//        long ticketsReopened =
//                reopenedHistory.size();


        double averageResolutionTimeHours =
                calculateAverageResolutionTime(resolvedHistory);


        return new HistoricalAnalyticsResponse(
                from,
                to,
                ticketsResolved,
                ticketsClosed,
                ticketsReopened,
                averageResolutionTimeHours
        );
    }
    
    
    
}