package com.supportai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supportai.entity.Ticket;
import com.supportai.entity.TicketCategory;
import com.supportai.entity.TicketPriority;
import com.supportai.entity.TicketStatus;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository
        extends JpaRepository<Ticket, Long>,JpaSpecificationExecutor<Ticket>  {

	List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

	List<Ticket> findByAssignedAgentIdOrderByCreatedAtDesc(Long agentId);

    List<Ticket> findByStatus(TicketStatus status);
    
    long countByStatus(TicketStatus status);
    
    
    long countByPriority(TicketPriority priority);

    long countByCategory(TicketCategory category);
    
    
    long countByAssignedAgentId(Long agentId);
    
    long countByAssignedAgentIsNull();

    long countByAssignedAgentIdAndStatus(
            Long agentId,
            TicketStatus status
    );

    long countByAssignedAgentIdAndPriority(
            Long agentId,
            TicketPriority priority
    );
    
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime from,
            LocalDateTime to
    );
    
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStatus(
            LocalDateTime from,
            LocalDateTime to,
            TicketStatus status
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndPriority(
            LocalDateTime from,
            LocalDateTime to,
            TicketPriority priority
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndCategory(
            LocalDateTime from,
            LocalDateTime to,
            TicketCategory category
    );
    
    long countByAssignedAgentIdAndCategoryAndStatus(
            Long agentId,
            TicketCategory category,
            TicketStatus status);
    
    List<Ticket> findBySlaDeadlineBeforeAndSlaBreachedFalse(
            LocalDateTime now);
    
    
    long countBySlaBreachedTrue();

    long countByEscalatedTrue();

    long countBySlaBreachedFalseAndSlaDeadlineAfter(
            LocalDateTime now);

    long countBySlaBreachedFalseAndSlaDeadlineBefore(
            LocalDateTime now);

    long countBySlaBreachedTrueAndPriority(
            TicketPriority priority);
    
    List<Ticket> findBySlaDeadlineIsNull();
    
    List<Ticket> findBySlaDeadlineBeforeAndSlaBreachedFalseAndStatusIn(
            LocalDateTime now,
            List<TicketStatus> statuses
    );
    
    List<Ticket> findByStatusIn(List<TicketStatus> statuses);
    
    
}