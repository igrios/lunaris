package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AgendaDateRangeTest {

    @Test
    void shiftsAcrossMonthsAndYearsWithAnInclusiveEnd() {
        var range = new AgendaDateRange(LocalDate.of(2026, 12, 30), 14);

        assertThat(range.shifted(-1)).isEqualTo(LocalDate.of(2026, 12, 29));
        assertThat(range.shifted(-7)).isEqualTo(LocalDate.of(2026, 12, 23));
        assertThat(range.shifted(1)).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(range.shifted(7)).isEqualTo(LocalDate.of(2027, 1, 6));
        assertThat(range.through()).isEqualTo(LocalDate.of(2027, 1, 12));
    }

    @Test
    void keepsDatesAndWindowWithinSupportedLimits() {
        var first = new AgendaDateRange(LocalDate.of(0, 12, 31), 1);
        var last = new AgendaDateRange(LocalDate.of(9999, 12, 31), Integer.MAX_VALUE);
        var full = new AgendaDateRange(LocalDate.of(2026, 12, 1), 56);

        assertThat(first.days()).isEqualTo(7);
        assertThat(first.from()).isEqualTo(first.firstDate());
        assertThat(first.shifted(-7)).isEqualTo(first.firstDate());
        assertThat(last.days()).isEqualTo(56);
        assertThat(last.shifted(7)).isEqualTo(last.lastDate());
        assertThat(last.through()).isEqualTo(last.lastDate());
        assertThat(last.canExpand()).isFalse();
        assertThat(full.canExpand()).isFalse();
    }
}
