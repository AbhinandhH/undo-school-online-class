package com.platform.booking.dto.response;

import com.platform.booking.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String timezone;
    private Instant createdAt;

    public static UserResponse buildUserResponse(User user) {
        return UserResponse.builder().
                id(user.getId()).
                name(user.getName()).
                email(user.getEmail()).
                role(user.getRole().name()).
                timezone(user.getTimezone()).
                createdAt(user.getCreatedAt()).
                build();
    }
}
