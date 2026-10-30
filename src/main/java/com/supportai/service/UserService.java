package com.supportai.service;

import com.supportai.dto.UserCreateRequest;
import com.supportai.dto.UserResponse;
import com.supportai.entity.Role;
import com.supportai.entity.User;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);
    
    UserResponse getUserByEmail(String email);
    
    UserResponse updateUserRole(Long userId, Role role);

}