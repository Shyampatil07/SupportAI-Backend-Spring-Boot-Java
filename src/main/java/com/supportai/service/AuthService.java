package com.supportai.service;

import com.supportai.dto.AuthResponse;
import com.supportai.dto.LoginRequest;
import com.supportai.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}