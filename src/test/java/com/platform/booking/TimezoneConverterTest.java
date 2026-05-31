package com.platform.booking;

import com.platform.booking.timezoneconfig.TimezoneConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TimezoneConverterTest {

    private final TimezoneConverter converter = new TimezoneConverter();

    @Test
    @DisplayName("New York 6 PM → correct UTC instant")
    void newYorkToUtc() {
        LocalDateTime nyTime = LocalDateTime.of(2030, 6, 7, 18, 0);
        Instant utc = converter.toUtc(nyTime, "America/New_York");


        Instant expected = LocalDateTime.of(2030, 6, 7, 22, 0)
                .atZone(ZoneId.of("UTC")).toInstant();
        assertThat(utc).isEqualTo(expected);
    }

    @Test
    @DisplayName("UTC → IST 6 PM = 11:30 PM IST")
    void utcToIst() {
        Instant utc = LocalDateTime.of(2030, 6, 7, 18, 0)
                .atZone(ZoneId.of("UTC")).toInstant();
        LocalDateTime ist = converter.toLocal(utc, "Asia/Kolkata");


        assertThat(ist).isEqualTo(LocalDateTime.of(2030, 6, 7, 23, 30));
    }

    @Test
    @DisplayName("Invalid timezone returns false")
    void invalidTimezone() {
        assertThat(converter.isValid("Mars/Olympus_Mons")).isFalse();
        assertThat(converter.isValid("Asia/Kolkata")).isTrue();
    }
}
