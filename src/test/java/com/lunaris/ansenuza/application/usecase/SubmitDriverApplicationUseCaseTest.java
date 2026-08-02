package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

class SubmitDriverApplicationUseCaseTest {

    @Test
    void storesAllDocumentsAndPersistsTheirLocations() {
        DriverApplicationRepository repository = mock(DriverApplicationRepository.class);
        DriverDocumentStoragePort storage = mock(DriverDocumentStoragePort.class);
        SubmitDriverApplicationUseCase useCase =
                new SubmitDriverApplicationUseCase(repository, storage);
        MockMultipartFile insurance = file("insuranceFile");
        MockMultipartFile greenCard = file("greenCardFile");
        MockMultipartFile criminalRecord = file("criminalRecordFile");
        when(storage.store("insurance", insurance)).thenReturn("/storage/insurance.pdf");
        when(storage.store("green-card", greenCard)).thenReturn("/storage/green-card.pdf");
        when(storage.store("criminal-record", criminalRecord))
                .thenReturn("/storage/criminal-record.pdf");
        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(
                new SubmitDriverApplicationUseCase.MultipartSubmission(
                        "Ana Pérez", "+54 9 351-234-5678", "Miramar",
                        "Renault Kangoo", 2022, "aa123bb", true),
                insurance,
                greenCard,
                criminalRecord);

        ArgumentCaptor<DriverApplication> captor =
                ArgumentCaptor.forClass(DriverApplication.class);
        verify(repository).save(captor.capture());
        DriverApplication application = captor.getValue();
        assertNull(application.getId());
        assertEquals("543512345678", application.getPhone());
        assertEquals("Miramar", application.getLocality());
        assertEquals("AA123BB", application.getLicensePlate());
        assertEquals("/storage/insurance.pdf", application.getInsuranceFileUrl());
        assertEquals("/storage/green-card.pdf", application.getGreenCardFileUrl());
        assertEquals("/storage/criminal-record.pdf", application.getCriminalRecordFileUrl());
    }

    @Test
    void updatesManagedApplicationForExistingNormalizedPhone() {
        DriverApplicationRepository repository = mock(DriverApplicationRepository.class);
        DriverDocumentStoragePort storage = mock(DriverDocumentStoragePort.class);
        SubmitDriverApplicationUseCase useCase =
                new SubmitDriverApplicationUseCase(repository, storage);
        UUID existingId = UUID.randomUUID();
        DriverApplication existing = DriverApplication.builder()
                .id(existingId)
                .fullName("Nombre anterior")
                .phone("543512345678")
                .locality("Morteros")
                .vehicleModel("Modelo anterior")
                .vehicleYear(2018)
                .licensePlate("OLD123")
                .wantsDirectContact(false)
                .insuranceFileUrl("/storage/existing-insurance.pdf")
                .status(DriverApplication.Status.REJECTED)
                .build();
        when(repository.findFirstByPhone("543512345678")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DriverApplication result = useCase.execute(
                new SubmitDriverApplicationUseCase.MultipartSubmission(
                        "Ana Pérez", "+54 9 351-234-5678", "Miramar",
                        "Renault Kangoo", 2022, "aa123bb", true),
                null,
                null,
                null);

        assertSame(existing, result);
        assertEquals(existingId, result.getId());
        assertEquals("Ana Pérez", result.getFullName());
        assertEquals("Miramar", result.getLocality());
        assertEquals("Renault Kangoo", result.getVehicleModel());
        assertEquals("AA123BB", result.getLicensePlate());
        assertEquals(DriverApplication.Status.PENDING, result.getStatus());
        assertEquals("/storage/existing-insurance.pdf", result.getInsuranceFileUrl());
        verify(repository).save(existing);
    }

    @Test
    void jsonSubmissionAlsoUpdatesExistingManagedApplication() {
        DriverApplicationRepository repository = mock(DriverApplicationRepository.class);
        DriverApplication existing = DriverApplication.builder()
                .id(UUID.randomUUID())
                .fullName("Nombre anterior")
                .phone("543511112222")
                .locality("Miramar")
                .vehicleModel("Modelo anterior")
                .vehicleYear(2019)
                .licensePlate("OLD123")
                .status(DriverApplication.Status.PENDING)
                .build();
        when(repository.findFirstByPhone("543511112222")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        SubmitDriverApplicationUseCase useCase = new SubmitDriverApplicationUseCase(
                repository, mock(DriverDocumentStoragePort.class));

        DriverApplication result = useCase.execute(new DriverApplicationRequest(
                "Juan Pérez", "+54 9 351-111-2222", "  Morteros  ",
                "Fiat Cronos", 2024, "ab123cd"));

        assertSame(existing, result);
        assertEquals("Juan Pérez", result.getFullName());
        assertEquals("Morteros", result.getLocality());
        assertEquals("Fiat Cronos", result.getVehicleModel());
        assertEquals("AB123CD", result.getLicensePlate());
        verify(repository).save(existing);
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile(name, name + ".pdf", "application/pdf", "pdf".getBytes());
    }
}
