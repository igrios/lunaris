package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private static final String SCHEDULE_MAX_CAPACITY = "schedule.max.capacity";
    private static final int DEFAULT_SCHEDULE_MAX_CAPACITY = 12;

    private final SystemConfigurationRepository repository;

    @Transactional(readOnly = true)
    public List<SystemConfiguration> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public SystemConfiguration findByKey(String key) {
        return repository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada: " + key));
    }

    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        if (key == null || key.isBlank()) {
            return defaultValue;
        }
        return repository.findById(key.trim())
                .map(SystemConfiguration::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getScheduleMaxCapacity() {
        String configuredValue = getValue(
                SCHEDULE_MAX_CAPACITY, String.valueOf(DEFAULT_SCHEDULE_MAX_CAPACITY));
        try {
            int capacity = Integer.parseInt(configuredValue.trim());
            return capacity > 0 ? capacity : DEFAULT_SCHEDULE_MAX_CAPACITY;
        } catch (NumberFormatException exception) {
            return DEFAULT_SCHEDULE_MAX_CAPACITY;
        }
    }

    @Transactional
    public SystemConfiguration save(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave de configuración es obligatoria.");
        }

        SystemConfiguration configuration = repository.findById(key.trim())
                .orElseGet(() -> SystemConfiguration.builder().key(key.trim()).build());
        configuration.setValue(value);
        return repository.saveAndFlush(configuration);
    }
}
