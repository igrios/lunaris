package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DriverApplicationNotFoundException;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverApplicationManagementService {

    private final DriverApplicationRepository applicationRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public List<DriverApplication> findPending() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(
                DriverApplication.Status.PENDING);
    }

    @Transactional
    public DriverApplication approve(UUID applicationId) {
        DriverApplication application = findApplication(applicationId);
        application.approve();

        Driver driver = driverRepository.findFirstByPhone(application.getPhone())
                .orElseGet(Driver::new);
        driver.setFullName(application.getFullName());
        driver.setPhone(application.getPhone());
        driver.setActive(true);
        if (driver.getId() == null) {
            driver.setId(UUID.randomUUID());
        }
        driverRepository.save(driver);

        return applicationRepository.save(application);
    }

    @Transactional
    public DriverApplication reject(UUID applicationId) {
        DriverApplication application = findApplication(applicationId);
        application.reject();
        return applicationRepository.save(application);
    }

    private DriverApplication findApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DriverApplicationNotFoundException(applicationId));
    }
}
