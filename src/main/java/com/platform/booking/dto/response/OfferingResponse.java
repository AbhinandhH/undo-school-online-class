package com.platform.booking.dto.response;

import com.platform.booking.entity.Offering;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OfferingResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private int maxCapacity;
    private long confirmedBookings;
    private String status;
    private List<SessionResponse> sessions;

    public static OfferingResponse buildOfferingResponse(Offering offering, List<SessionResponse> sessions, long confirmed) {
        return OfferingResponse.builder()
                .id(offering.getId())
                .courseId(offering.getCourse().getId())
                .courseTitle(offering.getCourse().getTitle())
                .teacherId(offering.getTeacher().getId())
                .teacherName(offering.getTeacher().getName())
                .title(offering.getTitle())
                .description(offering.getDescription())
                .maxCapacity(offering.getMaxCapacity())
                .confirmedBookings(confirmed)
                .status(offering.getStatus().name())
                .sessions(sessions)
                .build();
    }
}
