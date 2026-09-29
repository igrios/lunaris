package com.lunaris.ansenuza.service.interurban;

import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.driver.enabled"}, havingValue = "true")
public class InterurbanDriverConfiguration {
    @Bean public DriverOperationsRepository interurbanDriverRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcDriverOperationsRepository(jdbc);
    }
    @Bean public DriverIdentityPort interurbanDriverIdentity(DriverRepository drivers) {
        return new ExistingDriverIdentityAdapter(drivers);
    }
    @Bean public CheckInService interurbanCheckIn(DriverOperationsRepository repository, DriverIdentityPort identity,
            @Value("${lunaris.interurban.driver.early-window:PT1H}") String early,
            @Value("${lunaris.interurban.driver.late-window:PT2H}") String late) {
        return new CheckInService(repository, identity, Clock.systemUTC(), Duration.parse(early), Duration.parse(late));
    }
    @Bean public DriverRouteSheetQuery interurbanRouteSheet(DriverOperationsRepository repository, DriverIdentityPort identity) {
        return new DriverRouteSheetQuery(repository, identity);
    }
}
