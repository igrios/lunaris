package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

public class DailyDriverPayoutJob {
    private final DriverSettlementService settlement;
    private final DriverPayoutReviewService review;
    private final Clock clock;
    public DailyDriverPayoutJob(DriverSettlementService settlement, DriverPayoutReviewService review, Clock clock) {
        this.settlement = settlement; this.review = review; this.clock = clock;
    }
    @Scheduled(cron = "0 0 22 * * *", zone = "America/Argentina/Cordoba")
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        settlement.settle(DriverSettlementService.latestCutoff(clock.instant()));
        review.dispatch(review.pending());
    }
}
