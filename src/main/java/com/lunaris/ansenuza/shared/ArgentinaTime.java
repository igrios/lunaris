package com.lunaris.ansenuza.shared;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ArgentinaTime {

    public static final ZoneId ZONE_ID = ZoneId.of("America/Argentina/Cordoba");
    private static final Clock CLOCK = Clock.system(ZONE_ID);

    private ArgentinaTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(CLOCK);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(CLOCK);
    }

    public static LocalTime currentTime() {
        return LocalTime.now(CLOCK);
    }
}
