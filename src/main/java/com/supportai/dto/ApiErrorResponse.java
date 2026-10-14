package com.supportai.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;
}