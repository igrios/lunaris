package com.lunaris.ansenuza.service.interurban;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class InterurbanConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(InterurbanConfiguration.class, InterurbanCatalog.class);

    @Test void absentFlagRequiresNoJdbcAndRegistersNothing() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(CapacityService.class)
                    .doesNotHaveBean(CapacityRepository.class).doesNotHaveBean(InterurbanCatalog.class);
        });
    }

    @Test void falseFlagRegistersNothing() {
        runner.withPropertyValues("lunaris.interurban.enabled=false").run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(CapacityService.class)
                    .doesNotHaveBean(CapacityRepository.class).doesNotHaveBean(InterurbanCatalog.class);
        });
    }

    @Test void enabledRegistersServiceAndAdapterWithoutQueryingDatabase() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        runner.withPropertyValues("lunaris.interurban.enabled=true")
                .withBean(NamedParameterJdbcTemplate.class, () -> jdbc).run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(CapacityService.class)
                            .hasSingleBean(CapacityRepository.class).hasSingleBean(InterurbanCatalog.class);
                    verifyNoInteractions(jdbc);
                });
    }
}
