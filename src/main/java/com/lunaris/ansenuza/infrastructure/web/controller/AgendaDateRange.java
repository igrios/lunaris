package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;

public record AgendaDateRange(LocalDate from, int days) {
    private static final LocalDate FIRST_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate LAST_DATE = LocalDate.of(9999, 12, 31);
    private static final int MAX_DAYS = 56;

    public AgendaDateRange {
        from = clamp(from);
        days = Math.clamp(days, 7, MAX_DAYS);
    }

    public LocalDate through() {
        return shifted(days - 1);
    }

    public LocalDate shifted(int offset) {
        return clamp(from.plusDays(offset));
    }

    public LocalDate firstDate() {
        return FIRST_DATE;
    }

    public LocalDate lastDate() {
        return LAST_DATE;
    }

    public boolean canExpand() {
        return days < MAX_DAYS && through().isBefore(LAST_DATE);
    }

    private static LocalDate clamp(LocalDate date) {
        if (date.isBefore(FIRST_DATE)) {
            return FIRST_DATE;
        }
        return date.isAfter(LAST_DATE) ? LAST_DATE : date;
    }
}
