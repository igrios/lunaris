package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    @Test
    void verifyOtpFallsBackToMultipartParametersWhenReservationPartIsMissing() throws Exception {
        PassengerOtpService otpService = mock(PassengerOtpService.class);
        ProcessPaymentReceiptUseCase receiptUseCase = mock(ProcessPaymentReceiptUseCase.class);
        when(otpService.verifyOtp("3515550000", "1234"))
                .thenReturn(new PassengerOtpService.TokenResult("token", Instant.parse("2026-08-02T12:00:00Z")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(otpService, receiptUseCase)).build();
        MockMultipartFile receipt = new MockMultipartFile(
                "receipt", "receipt.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/auth/verify-otp")
                        .file(receipt)
                        .param("phone", "3515550000")
                        .param("code", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));

        verify(receiptUseCase).confirmOrCreateWebBooking(eq("3515550000"), eq(receipt), any());
    }

    @Test
    void verifyOtpAcceptsOptionalReservationAndReceiptParts() throws Exception {
        PassengerOtpService otpService = mock(PassengerOtpService.class);
        ProcessPaymentReceiptUseCase receiptUseCase = mock(ProcessPaymentReceiptUseCase.class);
        when(otpService.verifyOtp("3515550000", "1234"))
                .thenReturn(new PassengerOtpService.TokenResult("token", Instant.parse("2026-08-02T12:00:00Z")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(otpService, receiptUseCase)).build();
        MockMultipartFile reservation = new MockMultipartFile(
                "reservation",
                "reservation.json",
                "application/json",
                "{\"phone\":\"3515550000\",\"code\":\"1234\"}".getBytes());

        mockMvc.perform(multipart("/api/auth/verify-otp").file(reservation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));

        verify(receiptUseCase).confirmOrCreateWebBooking(eq("3515550000"), eq(null), any());
    }
}
