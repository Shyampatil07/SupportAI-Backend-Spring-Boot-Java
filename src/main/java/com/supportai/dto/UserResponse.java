package com.supportai.dto;

import java.time.LocalDateTime;

import com.supportai.entity.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private LocalDateTime createdAt;
}