package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
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
        when(repository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(
                new SubmitDriverApplicationUseCase.MultipartSubmission(
                        "Ana Pérez", "543512345678", "Miramar",
                        "Renault Kangoo", 2022, "aa123bb", true),
                insurance,
                greenCard,
                criminalRecord);

        ArgumentCaptor<DriverApplication> captor =
                ArgumentCaptor.forClass(DriverApplication.class);
        verify(repository).save(captor.capture());
        DriverApplication application = captor.getValue();
        assertEquals("AA123BB", application.getLicensePlate());
        assertEquals("/storage/insurance.pdf", application.getInsuranceFileUrl());
        assertEquals("/storage/green-card.pdf", application.getGreenCardFileUrl());
        assertEquals("/storage/criminal-record.pdf", application.getCriminalRecordFileUrl());
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile(name, name + ".pdf", "application/pdf", "pdf".getBytes());
    }
}
