package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SystemConfigurationServiceTest {

    @Test
    void returnsDefaultForMissingBlankOrNullConfiguration() {
        SystemConfigurationRepository repository =
                mock(SystemConfigurationRepository.class);
        SystemConfiguration configuration = SystemConfiguration.builder()
                .key("driver.setting")
                .value(null)
                .build();
        when(repository.findById("missing")).thenReturn(Optional.empty());
        when(repository.findById("driver.setting"))
                .thenReturn(Optional.of(configuration));
        SystemConfigurationService service =
                new SystemConfigurationService(repository);

        assertEquals("default", service.getValue(null, "default"));
        assertEquals("default", service.getValue(" ", "default"));
        assertEquals("default", service.getValue("missing", "default"));
        assertEquals("default", service.getValue("driver.setting", "default"));
        assertEquals(12, service.getScheduleMaxCapacity());
    }

    @Test
    void readsPositiveScheduleCapacityAndFallsBackForInvalidValue() {
        SystemConfigurationRepository repository = mock(SystemConfigurationRepository.class);
        SystemConfigurationService service = new SystemConfigurationService(repository);
        when(repository.findById("schedule.max.capacity")).thenReturn(Optional.of(
                SystemConfiguration.builder()
                        .key("schedule.max.capacity")
                        .value("20")
                        .build()));

        assertEquals(20, service.getScheduleMaxCapacity());

        when(repository.findById("schedule.max.capacity")).thenReturn(Optional.of(
                SystemConfiguration.builder()
                        .key("schedule.max.capacity")
                        .value("invalid")
                        .build()));
        assertEquals(12, service.getScheduleMaxCapacity());
    }
}
