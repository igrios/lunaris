package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

class DriverApplicationApiContractTest {

    private SubmitDriverApplicationUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(SubmitDriverApplicationUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new DriverApplicationApiController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void acceptsExactMultipartContractAndReturnsCreatedJson() throws Exception {
        UUID applicationId = UUID.randomUUID();
        DriverApplication saved = DriverApplication.builder()
                .id(applicationId)
                .fullName("Ana Pérez")
                .phone("543512345678")
                .locality("Miramar")
                .vehicleModel("Renault Kangoo")
                .vehicleYear(2022)
                .licensePlate("AA123BB")
                .wantsDirectContact(true)
                .status(DriverApplication.Status.PENDING)
                .build();
        when(useCase.execute(
                any(SubmitDriverApplicationUseCase.MultipartSubmission.class),
                any(MultipartFile.class),
                any(MultipartFile.class),
                any(MultipartFile.class)))
                .thenReturn(saved);

        MockMultipartFile insurance = file("insuranceFile", "seguro.pdf");
        MockMultipartFile greenCard = file("greenCardFile", "cedula.pdf");
        MockMultipartFile criminalRecord = file("criminalRecordFile", "antecedentes.pdf");

        mockMvc.perform(multipart("/api/drivers/applications")
                        .file(insurance)
                        .file(greenCard)
                        .file(criminalRecord)
                        .param("fullName", "Ana Pérez")
                        .param("phone", "543512345678")
                        .param("locality", "Miramar")
                        .param("vehicleModel", "Renault Kangoo")
                        .param("vehicleYear", "2022")
                        .param("plateNumber", "AA123BB")
                        .param("wantsDirectContact", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(applicationId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.fullName").value("Ana Pérez"))
                .andExpect(jsonPath("$.phone").value("543512345678"))
                .andExpect(jsonPath("$.locality").value("Miramar"))
                .andExpect(jsonPath("$.vehicleModel").value("Renault Kangoo"))
                .andExpect(jsonPath("$.vehicleYear").value(2022))
                .andExpect(jsonPath("$.plateNumber").value("AA123BB"))
                .andExpect(jsonPath("$.wantsDirectContact").value(true));

        ArgumentCaptor<SubmitDriverApplicationUseCase.MultipartSubmission> submissionCaptor =
                ArgumentCaptor.forClass(SubmitDriverApplicationUseCase.MultipartSubmission.class);
        verify(useCase).execute(
                submissionCaptor.capture(),
                any(MultipartFile.class),
                any(MultipartFile.class),
                any(MultipartFile.class));
        org.junit.jupiter.api.Assertions.assertEquals(
                "Miramar", submissionCaptor.getValue().locality());
    }

    private MockMultipartFile file(String field, String name) {
        return new MockMultipartFile(
                field, name, MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
    }
}
