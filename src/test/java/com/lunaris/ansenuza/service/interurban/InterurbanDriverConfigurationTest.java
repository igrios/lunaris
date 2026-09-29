package com.lunaris.ansenuza.service.interurban;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class InterurbanDriverConfigurationTest {
    final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            InterurbanDriverConfiguration.class, InterurbanDriverController.class, InterurbanDriverExceptionHandler.class);

    @Test void disabledByDefaultWithoutRequiringRepositories() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CheckInService.class)
                .doesNotHaveBean(InterurbanDriverController.class));
    }

    @Test void generalModuleFlagAloneDoesNotEnableDriverOperations() {
        runner.withPropertyValues("lunaris.interurban.enabled=true").run(context -> assertThat(context)
                .hasNotFailed().doesNotHaveBean(CheckInService.class));
    }

    @Test void bothFlagsRegisterServicesWithoutQueryingTheDatabase() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var drivers = mock(DriverRepository.class);
        runner.withPropertyValues("lunaris.interurban.enabled=true", "lunaris.interurban.driver.enabled=true")
                .withBean(NamedParameterJdbcTemplate.class, () -> jdbc).withBean(DriverRepository.class, () -> drivers)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(CheckInService.class).hasSingleBean(DriverRouteSheetQuery.class);
                    verifyNoInteractions(jdbc, drivers);
                });
    }
}
