package com.lunaris.ansenuza.service.interurban;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class InterurbanDriverConfigurationTest {
    final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            InterurbanDriverConfiguration.class, InterurbanDriverController.class, InterurbanDriverExceptionHandler.class);

    @Test void disabledByDefaultWithoutRequiringRepositories() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CheckInService.class)
                .doesNotHaveBean(InterurbanDriverController.class));
    }

    @Test void explicitFalseDisablesDriverOperations() {
        runner.withPropertyValues("lunaris.interurban.enabled=false").run(context -> assertThat(context)
                .hasNotFailed().doesNotHaveBean(CheckInService.class).doesNotHaveBean(InterurbanDriverController.class));
    }

    @Test void generalFlagRegistersControllerAndServicesWithoutQueryingTheDatabase() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var drivers = mock(DriverRepository.class);
        runner.withPropertyValues("lunaris.interurban.enabled=true")
                .withBean(NamedParameterJdbcTemplate.class, () -> jdbc).withBean(DriverRepository.class, () -> drivers)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(CheckInService.class).hasSingleBean(DriverRouteSheetQuery.class).hasSingleBean(InterurbanDriverController.class);
                    verifyNoInteractions(jdbc, drivers);
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"application.yaml", "application-prod.yml"})
    void environmentVariableEnablesDriverEndpointsThroughYaml(String resource) {
        runner.withInitializer(context -> {
                    try {
                        var sources = context.getEnvironment().getPropertySources();
                        var yaml = new YamlPropertySourceLoader().load("config",
                                new ClassPathResource(resource));
                        yaml.forEach(sources::addLast);
                        sources.addFirst(new SystemEnvironmentPropertySource(
                                "render", Map.of("LUNARIS_INTERURBAN_ENABLED", "true")));
                    } catch (IOException e) { throw new UncheckedIOException(e); }
                })
                .withBean(NamedParameterJdbcTemplate.class, () -> mock(NamedParameterJdbcTemplate.class))
                .withBean(DriverRepository.class, () -> mock(DriverRepository.class))
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(InterurbanDriverController.class));
    }
}
