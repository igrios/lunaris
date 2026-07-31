package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DriverApplicationManagementServiceTest {

    private final DriverApplicationRepository applicationRepository =
            mock(DriverApplicationRepository.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final DriverApplicationManagementService service =
            new DriverApplicationManagementService(applicationRepository, driverRepository);

    @Test
    void approveCreatesAnActiveDriver() {
        UUID applicationId = UUID.randomUUID();
        DriverApplication application = pendingApplication(applicationId);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(driverRepository.findFirstByPhone(application.getPhone())).thenReturn(Optional.empty());
        when(applicationRepository.save(application)).thenReturn(application);

        DriverApplication result = service.approve(applicationId);

        assertEquals(DriverApplication.Status.APPROVED, result.getStatus());
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertTrue(driverCaptor.getValue().isActive());
        assertEquals(application.getFullName(), driverCaptor.getValue().getFullName());
    }

    @Test
    void approveReactivatesExistingDriver() {
        UUID applicationId = UUID.randomUUID();
        DriverApplication application = pendingApplication(applicationId);
        Driver existing = new Driver();
        existing.setActive(false);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(driverRepository.findFirstByPhone(application.getPhone()))
                .thenReturn(Optional.of(existing));
        when(applicationRepository.save(application)).thenReturn(application);

        service.approve(applicationId);

        assertTrue(existing.isActive());
        verify(driverRepository).save(existing);
    }

    @Test
    void rejectChangesPendingApplicationStatus() {
        UUID applicationId = UUID.randomUUID();
        DriverApplication application = pendingApplication(applicationId);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        DriverApplication result = service.reject(applicationId);

        assertEquals(DriverApplication.Status.REJECTED, result.getStatus());
    }

    private DriverApplication pendingApplication(UUID id) {
        return DriverApplication.builder()
                .id(id)
                .fullName("Ada Lovelace")
                .phone("3515550000")
                .locality("Miramar")
                .vehicleModel("Sprinter")
                .licensePlate("AA123BB")
                .status(DriverApplication.Status.PENDING)
                .build();
    }
}
