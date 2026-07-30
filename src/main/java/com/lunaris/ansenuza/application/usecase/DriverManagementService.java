package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverManagementService {

    private final DriverRepository repository;

    @Transactional(readOnly = true)
    public List<Driver> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Driver create(String fullName, String phone, Integer ranking, Boolean active) {
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        mapFields(driver, fullName, phone, ranking, active);
        return repository.saveAndFlush(driver);
    }

    private void mapFields(
            Driver driver, String fullName, String phone, Integer ranking, Boolean active) {
        driver.setFullName(requiredText(fullName, "El nombre del chofer es obligatorio."));
        driver.setPhone(requiredText(phone, "El teléfono del chofer es obligatorio."));
        if (ranking != null && (ranking < 1 || ranking > 5)) {
            throw new DomainValidationException("El ranking debe estar entre 1 y 5.");
        }
        driver.setRanking(ranking);
        driver.setActive(active == null || active);
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
        return value.trim();
    }
}
