package com.supportai.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketCommentResponse {

    private Long id;

    private Long ticketId;

    private Long userId;

    private String userName;

    private String userRole;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}