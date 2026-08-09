package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import java.time.Instant;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

class AuthControllerTest {

    @Test
    void verifyOtpFallsBackToMultipartParametersWhenReservationPartIsMissing() throws Exception {
        PassengerOtpService otpService = mock(PassengerOtpService.class);
        ProcessPaymentReceiptUseCase receiptUseCase = mock(ProcessPaymentReceiptUseCase.class);
        CreateReservationUseCase createReservationUseCase = mock(CreateReservationUseCase.class);
        when(otpService.verifyOtp("3515550000", "1234"))
                .thenReturn(new PassengerOtpService.TokenResult("token", Instant.parse("2026-08-02T12:00:00Z")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(otpService, receiptUseCase, createReservationUseCase)).build();
        MockMultipartFile receipt = new MockMultipartFile(
                "receipt", "receipt.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/auth/verify-otp")
                        .file(receipt)
                        .param("phone", "3515550000")
                        .param("code", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
        verify(createReservationUseCase, never()).execute(any(CreateReservationRequest.class));
        verify(receiptUseCase).confirmOrCreateWebBooking(eq("3515550000"), eq(receipt), any());
    }

    @Test
    void verifyOtpAcceptsOptionalReservationAndReceiptParts() throws Exception {
        PassengerOtpService otpService = mock(PassengerOtpService.class);
        ProcessPaymentReceiptUseCase receiptUseCase = mock(ProcessPaymentReceiptUseCase.class);
        CreateReservationUseCase createReservationUseCase = mock(CreateReservationUseCase.class);
        when(otpService.verifyOtp("3515550000", "1234"))
                .thenReturn(new PassengerOtpService.TokenResult("token", Instant.parse("2026-08-02T12:00:00Z")));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(otpService, receiptUseCase, createReservationUseCase)).build();
        MockMultipartFile reservation = new MockMultipartFile(
                "reservation",
                "reservation.json",
                "application/json",
                "{\"phone\":\"3515550000\",\"code\":\"1234\"}".getBytes());

        mockMvc.perform(multipart("/api/auth/verify-otp").file(reservation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
        verify(createReservationUseCase, never()).execute(any(CreateReservationRequest.class));
    }

    @Test
    void verifyOtpCreatesReservationWithTheVerifiedBookingPayload() throws Exception {
        PassengerOtpService otpService = mock(PassengerOtpService.class);
        ProcessPaymentReceiptUseCase receiptUseCase = mock(ProcessPaymentReceiptUseCase.class);
        CreateReservationUseCase createReservationUseCase = mock(CreateReservationUseCase.class);
        when(otpService.verifyOtp("5493515550000", "1234"))
                .thenReturn(new PassengerOtpService.TokenResult("token", Instant.parse("2026-08-02T12:00:00Z")));
        Reservation reservation = Reservation.builder()
                .reservationCode("MOR-COR-001")
                .bookingGroupCode("MOR-COR-001")
                .build();
        when(createReservationUseCase.execute(any(CreateReservationRequest.class))).thenReturn(reservation);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(otpService, receiptUseCase, createReservationUseCase)).build();

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"5493515550000","code":"1234","fullName":"Ana Pérez",
                                 "cuilDni":"30111222","travelDate":"2026-08-20",
                                 "departureSchedule":"08:00 AM","pickupLocality":"Morteros",
                                 "pickupAddress":"San Martín 123","destination":"Córdoba",
                                 "passengerCount":2,"companionNames":"Luis Pérez","tripType":"ONE_WAY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").value("MOR-COR-001"));

        ArgumentCaptor<CreateReservationRequest> request =
                ArgumentCaptor.forClass(CreateReservationRequest.class);
        verify(createReservationUseCase).execute(request.capture());
        org.junit.jupiter.api.Assertions.assertEquals("30111222", request.getValue().cuilDni());
        org.junit.jupiter.api.Assertions.assertEquals("08:00 AM", request.getValue().departureSchedule());
        org.junit.jupiter.api.Assertions.assertEquals("San Martín 123", request.getValue().pickupAddress());
    }
}
