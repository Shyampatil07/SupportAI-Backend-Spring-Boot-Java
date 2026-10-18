package com.supportai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
	
	 private Long conversationId;

    @NotBlank(message = "Message is required")
    private String message;
}