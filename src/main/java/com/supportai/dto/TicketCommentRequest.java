package com.supportai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String content;
}