package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.settlement.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanSettlementConfiguration {
    @Bean DriverSettlementService driverSettlementService(NamedParameterJdbcTemplate jdbc) {
        return new DriverSettlementService(jdbc, Clock.systemUTC());
    }
    @Bean DriverPayoutPort driverPayoutPort() { return new SimulatedDriverPayoutAdapter(); }
    @Bean DriverPayoutReviewService driverPayoutReviewService(NamedParameterJdbcTemplate jdbc, DriverPayoutPort port) {
        return new DriverPayoutReviewService(jdbc, port);
    }
    @Bean DailyDriverPayoutJob dailyDriverPayoutJob(DriverSettlementService service, DriverPayoutReviewService review) {
        return new DailyDriverPayoutJob(service, review, Clock.systemUTC());
    }
}
