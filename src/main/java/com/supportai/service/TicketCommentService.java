package com.supportai.service;

import java.util.List;

import com.supportai.dto.TicketCommentRequest;
import com.supportai.dto.TicketCommentResponse;

public interface TicketCommentService {

    TicketCommentResponse addComment(
            Long ticketId,
            TicketCommentRequest request,
            String userEmail);

    List<TicketCommentResponse> getComments(
            Long ticketId,
            String userEmail);
}