package com.supportai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supportai.entity.TicketHistory;
import com.supportai.entity.TicketHistoryAction;

public interface TicketHistoryRepository
        extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(
            Long ticketId);
    
    
    List<TicketHistory> findByActionAndNewValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            TicketHistoryAction action,
            String newValue,
            LocalDateTime from,
            LocalDateTime to
    );
    
    List<TicketHistory> findByActionAndOldValueAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            TicketHistoryAction action,
            String oldValue,
            LocalDateTime from,
            LocalDateTime to
    );
    
    List<TicketHistory> findByActionOrderByCreatedAtAsc(
            TicketHistoryAction action
    );
    
    
}