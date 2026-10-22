package com.supportai.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.SlaStatusResponse;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketPriority;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.TicketRepository;
import com.supportai.service.SlaService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SlaServiceImpl implements SlaService {

    private final TicketRepository ticketRepository;

    @Override
    public LocalDateTime calculateDeadline(
            LocalDateTime createdAt,
            TicketPriority priority) {

        int hours = switch (priority) {
            case LOW -> 48;
            case MEDIUM -> 24;
            case HIGH -> 8;
            case CRITICAL -> 4;
        };

        return createdAt.plusHours(hours);
    }

    @Override
    @Transactional(readOnly = true)
    public SlaStatusResponse getSlaStatus(
            Long ticketId,
            String userEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + ticketId));
        
        if (ticket.getSlaDeadline() == null) {

            LocalDateTime deadline =
                    calculateDeadline(
                            ticket.getCreatedAt(),
                            ticket.getPriority());

            ticket.setSlaDeadline(deadline);
            ticket.setSlaBreached(false);

            ticketRepository.save(ticket);
        }

        LocalDateTime now = LocalDateTime.now();

        boolean breached =
                ticket.isSlaBreached()
                || now.isAfter(ticket.getSlaDeadline());

        long remainingMinutes = 0;

        if (!breached) {
            remainingMinutes =
                    Duration.between(
                            now,
                            ticket.getSlaDeadline()
                    ).toMinutes();
        }

        String status;

        if (breached) {
            status = "BREACHED";
        } else if (remainingMinutes <= 60) {
            status = "AT_RISK";
        } else {
            status = "ON_TRACK";
        }

        return new SlaStatusResponse(
                ticket.getId(),
                ticket.getSlaDeadline(),
                breached,
                remainingMinutes,
                status
        );
    }
}