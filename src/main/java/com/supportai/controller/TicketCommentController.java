package com.supportai.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supportai.dto.TicketCommentRequest;
import com.supportai.dto.TicketCommentResponse;
import com.supportai.service.TicketCommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;


    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<TicketCommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketCommentRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TicketCommentResponse response =
                ticketCommentService.addComment(
                        ticketId,
                        request,
                        userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<TicketCommentResponse>> getComments(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<TicketCommentResponse> comments =
                ticketCommentService.getComments(
                        ticketId,
                        userEmail);

        return ResponseEntity.ok(comments);
    }
}