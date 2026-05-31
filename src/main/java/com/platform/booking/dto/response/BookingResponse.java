package com.platform.booking.dto.response;

import com.platform.booking.entity.Booking;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long offeringId;
    private String offeringTitle;
    private Long parentId;
    private String parentName;
    private String status;
    private Instant bookedAt;
    private List<SessionResponse> sessions;

    public static BookingResponse buildBookingResponse(Booking booking, List<SessionResponse> sessions) {
        return BookingResponse.builder()
                .id(booking.getId())
                .offeringId(booking.getOffering().getId())
                .offeringTitle(booking.getOffering().getTitle())
                .parentId(booking.getParent().getId())
                .parentName(booking.getParent().getName())
                .status(booking.getStatus().name())
                .bookedAt(booking.getBookedAt())
                .sessions(sessions)
                .build();
    }
}
