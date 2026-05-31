package com.platform.booking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequest {

    @NotBlank(message = "name should not be empty")
    @Size(max = 50)
    private String name;

    @Email(message = "enter a valid email id")
    @NotBlank(message = "email is mandatory")
    @Size(max = 70)
    private String email;

    @NotNull(message = "role is mandatory (PARENT/TEACHER)")
    private String role;

    @NotBlank(message = "timezone is mandatory eg:- Asia/Kolkata")
    private String timezone;



}
