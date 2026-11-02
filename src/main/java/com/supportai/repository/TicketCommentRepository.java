package com.supportai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supportai.entity.TicketComment;

public interface TicketCommentRepository
        extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(
            Long ticketId);
}