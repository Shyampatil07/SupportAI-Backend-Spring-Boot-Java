package com.supportai.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.entity.Role;
import com.supportai.entity.Ticket;
import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;
import com.supportai.entity.User;
import com.supportai.enums.NotificationType;
import com.supportai.repository.TicketHistoryRepository;
import com.supportai.repository.UserRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.service.NotificationService;
import com.supportai.service.SlaEscalationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlaEscalationServiceImpl
        implements SlaEscalationService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void escalate(Ticket ticket) {

        if (ticket.isEscalated()) {
            return;
        }

        ticket.setEscalated(true);
        ticket.setEscalatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        // Manager lookup
        List<User> managers =
                userRepository.findByRole(Role.MANAGER);

        // Add escalation history
        if (!managers.isEmpty()) {

//            User manager = managers.get(0);
        	for (User manager : managers) {
        		
        		 TicketHistory history = TicketHistory.builder()
                    .ticket(ticket)
                    .changedBy(manager)
                    .action(TicketHistoryAction.SLA_BREACHED)
                    .oldValue("SLA_ACTIVE")
                    .newValue("SLA_ESCALATED")
                    .build();
        		 ticketHistoryRepository.save(history);

        	    notificationService.createNotification(
        	            manager.getId(),
        	            "SLA Breached",
        	            "Ticket #" + ticket.getId()
        	                    + " has breached its SLA and requires attention.",
        	            NotificationType.SLA_BREACHED
        	    );  
        	    
        	}
           

          
            
//            notificationService.createNotification(
//                    manager.getId(),
//                    "SLA Breached",
//                    "Ticket #" + ticket.getId()
//                            + " has breached its SLA and requires attention.",
//                    NotificationType.SLA_BREACHED
           // );
        }
    }
}