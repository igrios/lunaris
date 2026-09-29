package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DriverSettlementTest {
    @Test void cutoffUsesCordobaAndJobReusesTheSameDate() {
        var service = mock(DriverSettlementService.class);
        var review = mock(DriverPayoutReviewService.class);
        var clock = Clock.fixed(Instant.parse("2026-09-30T01:00:00Z"), ZoneOffset.UTC);
        var job = new DailyDriverPayoutJob(service, review, clock);
        job.run(); job.run();
        verify(service, times(2)).settle(LocalDate.of(2026, 9, 29));
        verify(review, times(2)).dispatch(List.of());
        assertEquals(LocalDate.of(2026, 9, 28), DriverSettlementService.latestCutoff(clock.instant().minusNanos(1)));
    }
    @Test void configurationIsDisabledUnlessBothFlagsAreEnabled() {
        var runner = new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withUserConfiguration(InterurbanSettlementConfiguration.class)
                .withBean(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class,
                        () -> mock(org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.class));
        runner.run(context -> assertFalse(context.containsBean("dailyDriverPayoutJob")));
        runner.withPropertyValues("lunaris.interurban.enabled=true")
                .run(context -> assertFalse(context.containsBean("dailyDriverPayoutJob")));
        runner.withPropertyValues("lunaris.interurban.enabled=true", "lunaris.interurban.settlement.enabled=true")
                .run(context -> assertNotNull(context.getBean(DailyDriverPayoutJob.class)));
    }
    @Test void simulatedSubmissionAndReconciliationNeverConfirmPayment() {
        var adapter = new SimulatedDriverPayoutAdapter();
        var order = new DriverPayoutPort.Order(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("90.13"), "stable-key");
        var first = adapter.submitBatch(List.of(order)).getFirst();
        assertEquals(first, adapter.submitBatch(List.of(order)).getFirst());
        assertEquals(first, adapter.reconcile(order.idempotencyKey()));
        assertEquals(DriverPayoutPort.Status.READY_FOR_APPROVAL, first.status());
        assertNull(first.providerReference());
    }
}
