package com.platform.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AddSessionsRequest {

    @NotEmpty(message = "session required")
    @Valid
    private List<SessionSlot> sessions;

    @Data
    public static class SessionSlot {
        @NotNull(message = "start time required")
        private LocalDateTime startTime;

        @NotNull(message = "endtime required")
        private LocalDateTime endTime;
    }
}
