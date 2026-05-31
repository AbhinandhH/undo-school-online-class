package com.platform.booking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateOfferingRequest {

    @NotNull(message = "cource id required")
    private Long courseId;

    @NotBlank(message = "title required")
    @Size(max = 200, message = "maximum allowed characters are 200")
    private String title;

    private String description;

    @Min(value = 1, message = "minimum one character") @Max(value = 500, message = "maximum 500 characters allowed")
    private int maxCapacity = 30;
}
