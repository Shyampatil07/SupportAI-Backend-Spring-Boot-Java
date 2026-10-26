package com.supportai.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supportai.dto.UserCreateRequest;
import com.supportai.dto.UserResponse;
import com.supportai.entity.Role;
import com.supportai.entity.User;
import com.supportai.exception.ResourceAlreadyExistsException;
import com.supportai.exception.ResourceNotFoundException;
import com.supportai.repository.UserRepository;
import com.supportai.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));

        user.setRole(role);

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Email already registered: " + request.getEmail()
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}