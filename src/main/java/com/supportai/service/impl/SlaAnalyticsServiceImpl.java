package com.supportai.service.impl;

import com.supportai.dto.SlaAnalyticsResponse;
import com.supportai.dto.SlaReportResponse;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;
import com.supportai.entity.TicketStatus;
import com.supportai.entity.TicketPriority;

import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.service.SlaAnalyticsService;
import com.supportai.specification.TicketSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlaAnalyticsServiceImpl implements SlaAnalyticsService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public SlaReportResponse getSlaReport(
            LocalDate from,
            LocalDate to,
            TicketPriority priority,
            TicketCategory category,
            String slaStatus,
            Boolean escalated) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' date cannot be after 'to' date"
            );
        }

        Specification<Ticket> specification =
                TicketSpecification.slaReportFilter(
                        from,
                        to,
                        priority,
                        category,
                        slaStatus,
                        escalated
                );

        List<Ticket> tickets =
                ticketRepository.findAll(specification);

        LocalDateTime now = LocalDateTime.now();

        long totalTickets = tickets.size();

        long breachedTickets =
                tickets.stream()
                        .filter(Ticket::isSlaBreached)
                        .count();

        long atRiskTickets =
                tickets.stream()
                        .filter(ticket ->
                                !ticket.isSlaBreached()
                                && ticket.getSlaDeadline() != null
                                && ticket.getSlaDeadline().isAfter(now)
                                && !ticket.getSlaDeadline()
                                        .isAfter(now.plusHours(1))
                        )
                        .count();

        long compliantTickets =
                tickets.stream()
                        .filter(ticket ->
                                !ticket.isSlaBreached()
                                && ticket.getSlaDeadline() != null
                                && ticket.getSlaDeadline()
                                        .isAfter(now.plusHours(1))
                        )
                        .count();

        long escalatedTickets =
                tickets.stream()
                        .filter(Ticket::isEscalated)
                        .count();

        long criticalBreaches =
                tickets.stream()
                        .filter(ticket ->
                                ticket.getPriority()
                                        == TicketPriority.CRITICAL
                                && ticket.isSlaBreached()
                        )
                        .count();

        double averageResolutionTimeHours =
                calculateAverageResolutionTime(tickets);

        return new SlaReportResponse(
                from,
                to,
                totalTickets,
                breachedTickets,
                atRiskTickets,
                compliantTickets,
                escalatedTickets,
                criticalBreaches,
                averageResolutionTimeHours
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public SlaAnalyticsResponse getSlaAnalytics() {

//        LocalDateTime now = LocalDateTime.now();

        // -----------------------------------------
        // ACTIVE TICKETS
        // -----------------------------------------

//        long openTickets =
//                ticketRepository.countByStatus(TicketStatus.OPEN);
//
//        long inProgressTickets =
//                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        
        List<Ticket> activeTickets =
                ticketRepository.findByStatusIn(
                        List.of(
                                TicketStatus.OPEN,
                                TicketStatus.IN_PROGRESS
                        )
                );
        long totalActiveTickets = activeTickets.size();

        long slaBreachedTickets =
                activeTickets.stream()
                        .filter(Ticket::isSlaBreached)
                        .count();

        long escalatedTickets =
                activeTickets.stream()
                        .filter(Ticket::isEscalated)
                        .count();

        long criticalBreaches =
                activeTickets.stream()
                        .filter(ticket ->
                                ticket.getPriority() == TicketPriority.CRITICAL
                                && ticket.isSlaBreached()
                        )
                        .count();

        // -----------------------------------------
        // SLA AT RISK
        // -----------------------------------------

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        long slaAtRiskTickets =
                activeTickets.stream()
                        .filter(ticket ->
                                !ticket.isSlaBreached()
                                && ticket.getSlaDeadline() != null
                                && ticket.getSlaDeadline().isAfter(now)
                                && !ticket.getSlaDeadline().isAfter(oneHourLater)
                        )
                        .count();


        // -----------------------------------------
        // SLA COMPLIANT
        // -----------------------------------------

        long slaCompliantTickets =
                activeTickets.stream()
                        .filter(ticket ->
                                !ticket.isSlaBreached()
                                && ticket.getSlaDeadline() != null
                                && ticket.getSlaDeadline().isAfter(oneHourLater)
                        )
                        .count();


        // -----------------------------------------
        // CRITICAL SLA BREACHES
        // -----------------------------------------

//        long criticalBreaches =
//                ticketRepository
//                        .countBySlaBreachedTrueAndPriority(
//                                TicketPriority.CRITICAL
//                        );


        // -----------------------------------------
        // AVERAGE RESOLUTION TIME
        // -----------------------------------------

        double averageResolutionTimeHours =
                calculateAverageResolutionTime(ticketRepository.findAll());


        return new SlaAnalyticsResponse(
                totalActiveTickets,
                slaBreachedTickets,
                slaAtRiskTickets,
                slaCompliantTickets,
                escalatedTickets,
                criticalBreaches,
                averageResolutionTimeHours
        );
    }


    private double calculateAverageResolutionTime(
            List<Ticket> filteredTickets) {

        if (filteredTickets.isEmpty()) {
            return 0.0;
        }

        Set<Long> ticketIds =
                filteredTickets.stream()
                        .map(Ticket::getId)
                        .collect(Collectors.toSet());

        List<TicketHistory> histories =
                ticketHistoryRepository
                        .findByActionOrderByCreatedAtAsc(
                                TicketHistoryAction.STATUS_CHANGED
                        );

        Map<Long, LocalDateTime> resolutionStartMap =
                new HashMap<>();

        double totalHours = 0.0;
        int resolutionCount = 0;

        for (TicketHistory history : histories) {

            Long ticketId =
                    history.getTicket().getId();

            if (!ticketIds.contains(ticketId)) {
                continue;
            }

            String oldValue = history.getOldValue();
            String newValue = history.getNewValue();

            // Ticket was reopened
            boolean reopened =
                    ("RESOLVED".equals(oldValue)
                            || "CLOSED".equals(oldValue))
                    &&
                    ("OPEN".equals(newValue)
                            || "IN_PROGRESS".equals(newValue));

            if (reopened) {

                resolutionStartMap.put(
                        ticketId,
                        history.getCreatedAt()
                );

                continue;
            }

            // Ticket was resolved
            if ("RESOLVED".equals(newValue)) {

                LocalDateTime resolutionStart =
                        resolutionStartMap.get(ticketId);

                // First resolution starts from ticket creation
                if (resolutionStart == null) {
                    resolutionStart =
                            history.getTicket().getCreatedAt();
                }

                Duration duration =
                        Duration.between(
                                resolutionStart,
                                history.getCreatedAt()
                        );

                if (!duration.isNegative()) {

                    totalHours +=
                            duration.toMinutes() / 60.0;

                    resolutionCount++;
                }

                // Remove start so a future reopen
                // can create a new resolution cycle
                resolutionStartMap.remove(ticketId);
            }
        }

        if (resolutionCount == 0) {
            return 0.0;
        }

        double average =
                totalHours / resolutionCount;

        return Math.round(average * 100.0) / 100.0;
    }
}