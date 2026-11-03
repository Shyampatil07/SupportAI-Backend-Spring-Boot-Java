package com.supportai.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.entity.Ticket;
import com.supportai.entity.TicketStatus;
import com.supportai.repository.TicketRepository;
import com.supportai.service.SlaBreachService;
import com.supportai.service.SlaEscalationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlaBreachServiceImpl
        implements SlaBreachService {

    private final TicketRepository ticketRepository;
    
    private final SlaEscalationService slaEscalationService;
    
    

    @Override
    @Transactional
    public void detectBreaches() {

        LocalDateTime now = LocalDateTime.now();

        List<Ticket> overdueTickets =
                ticketRepository
                        .findBySlaDeadlineBeforeAndSlaBreachedFalseAndStatusIn(
                                now,
                                List.of(
                                        TicketStatus.OPEN,
                                        TicketStatus.IN_PROGRESS
                                )
                        );

        for (Ticket ticket : overdueTickets) {

            ticket.setSlaBreached(true);

            ticketRepository.save(ticket);

            slaEscalationService.escalate(ticket);
        }
    }
}
