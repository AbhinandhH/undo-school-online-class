package com.platform.booking.dto.response;

import com.platform.booking.entity.Session;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class SessionResponse {
    private Long id;
    private Long offeringId;
    private Long teacherId;

    private Instant startTimeUtc;
    private Instant endTimeUtc;

    private ZonedDateTime startTimeLocal;
    private ZonedDateTime endTimeLocal;
    private String displayTimezone;

    public static SessionResponse buildSessionResponse(Session session, String viewerTimezone) {
        ZoneId zone = ZoneId.of(viewerTimezone);
        return SessionResponse.builder()
                .id(session.getId())
                .offeringId(session.getOffering().getId())
                .teacherId(session.getTeacher().getId())
                .startTimeUtc(session.getStartTimeUtc())
                .endTimeUtc(session.getEndTimeUtc())
                .startTimeLocal(session.getStartTimeUtc().atZone(zone))
                .endTimeLocal(session.getEndTimeUtc().atZone(zone))
                .displayTimezone(viewerTimezone)
                .build();
    }
}
