package com.platform.booking.service;

import com.platform.booking.dto.request.UserRequest;
import com.platform.booking.dto.response.UserResponse;
import com.platform.booking.entity.User;
import com.platform.booking.exception.ResourceNotFoundException;
import com.platform.booking.repo.UserRepo;
import com.platform.booking.timezoneconfig.TimezoneConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final TimezoneConverter timezoneConverter;

    public UserService(UserRepo userRepo, TimezoneConverter timezoneConverter) {
        this.userRepo = userRepo;
        this.timezoneConverter = timezoneConverter;
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        User.Role role;

        try {
            role = User.Role.valueOf(userRequest.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Only TEACHER/PARENT is allowed.");
        }

        if (!timezoneConverter.isValid(userRequest.getTimezone())) {
            throw new IllegalArgumentException("Invalid timezone: " + userRequest.getTimezone() + ". Format e.g:- Asia/Kolkata");
        }

        if(userRepo.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exist" + userRequest.getEmail());
        }

        User user = User.builder()
                .name(userRequest.getName())
                .createdAt(Instant.now())
                .email(userRequest.getEmail())
                .role(role)
                .timezone(userRequest.getTimezone())
                .build();

        return UserResponse.buildUserResponse(userRepo.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("This User", id));
        return UserResponse.buildUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> userList = userRepo.findAll();

        return userList.stream().map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .timezone(u.getTimezone())
                        .email(u.getEmail())
                        .createdAt(u.getCreatedAt())
                        .role(u.getRole().name()).build())
                .collect(Collectors.toList());
    }


}
