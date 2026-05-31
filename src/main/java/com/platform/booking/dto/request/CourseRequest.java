package com.platform.booking.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class CourseRequest {
    private Long id;

    @NotBlank(message = "title should not be empty")
    private String title;

    @NotBlank(message = "description should not be empty")
    private String description;

    private Instant createdAt;
}
