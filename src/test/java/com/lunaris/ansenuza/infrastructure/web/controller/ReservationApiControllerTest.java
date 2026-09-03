package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.PersistPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import com.lunaris.ansenuza.infrastructure.config.PassengerBearerAuthenticationFilter;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationApiController.class)
@Import({SecurityConfig.class, PassengerBearerAuthenticationFilter.class})
class ReservationApiControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CreateReservationUseCase createReservationUseCase;
    @MockitoBean ReceiptStoragePort receiptStoragePort;
    @MockitoBean PersistPaymentReceiptUseCase persistPaymentReceiptUseCase;
    @MockitoBean PassengerOtpService passengerOtpService;
    @MockitoBean AccountRepository accountRepository;

    @Test
    void receiptUploadRequiresPassengerAuthentication() throws Exception {
        mockMvc.perform(receiptRequest())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedPassengerCannotUploadReceiptForAnotherPassenger() throws Exception {
        when(passengerOtpService.resolvePhone("valid-token"))
                .thenReturn(Optional.of("543512282251"));
        when(receiptStoragePort.uploadFile(org.mockito.ArgumentMatchers.any()))
                .thenReturn("https://example.test/receipt.jpg");
        doThrow(new AccessDeniedException("La reserva no pertenece al pasajero autenticado."))
                .when(persistPaymentReceiptUseCase)
                .executeByReservationCodeOwnedBy(
                        anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(receiptRequest().header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
            receiptRequest() {
        return multipart("/api/v1/reservations/{reservationCode}/receipt", "MOR-COR-001-IDA")
                .file(new MockMultipartFile(
                        "file", "receipt.jpg", "image/jpeg", new byte[] {1, 2, 3}));
    }
}
