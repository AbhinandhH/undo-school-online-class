package com.platform.booking.timezoneconfig;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimezoneConverter {
    public Instant toUtc(LocalDateTime localDateTime, String ianaTimezone) {
        ZoneId zone = ZoneId.of(ianaTimezone);
        return localDateTime.atZone(zone).toInstant();
    }

    public LocalDateTime toLocal(Instant utc, String ianaTimezone) {
        ZoneId zone = ZoneId.of(ianaTimezone);
        return utc.atZone(zone).toLocalDateTime();
    }

    public boolean isValid(String ianaTimezone) {
        try {
            ZoneId.of(ianaTimezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
