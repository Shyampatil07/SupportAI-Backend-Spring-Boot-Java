package com.supportai.service.impl;

import com.supportai.entity.Ticket;
import com.supportai.repository.TicketRepository;
import com.supportai.service.SlaBackfillService;
import com.supportai.service.SlaService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaBackfillServiceImpl implements SlaBackfillService {

    private final TicketRepository ticketRepository;
    private final SlaService slaService;

    @Override
    @Transactional
    public int backfillSlaDeadlines() {

        List<Ticket> tickets =
                ticketRepository.findBySlaDeadlineIsNull();

        for (Ticket ticket : tickets) {

            LocalDateTime deadline =
                    slaService.calculateDeadline(
                            ticket.getCreatedAt(),
                            ticket.getPriority()
                    );

            ticket.setSlaDeadline(deadline);

            ticket.setSlaBreached(
                    LocalDateTime.now().isAfter(deadline)
            );
        }

        ticketRepository.saveAll(tickets);

        return tickets.size();
    }
}