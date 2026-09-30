package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanConfiguration {
    @Bean
    public CapacityRepository interurbanCapacityRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcCapacityRepository(jdbc);
    }

    @Bean
    public CapacityService interurbanCapacityService(CapacityRepository repository) {
        return new CapacityService(repository, Clock.systemUTC());
    }
}
