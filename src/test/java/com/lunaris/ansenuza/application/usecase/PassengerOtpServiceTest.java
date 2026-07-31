package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PassengerOtpServiceTest {

    @Test
    void sendsOtpThroughWhatsAppForRegisteredPassenger() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Passenger passenger = Passenger.builder()
                .firstName("Ana")
                .lastName("Pérez")
                .phone("+5493515555555")
                .build();
        when(passengers.findByPhone("+5493515555555")).thenReturn(Optional.of(passenger));

        PassengerOtpService service = new PassengerOtpService(
                passengers, messaging, Duration.ofMinutes(5), Duration.ofHours(12));

        service.sendOtp("+5493515555555");

        verify(messaging).sendText(
                eq("543515555555"),
                matches("Tu código de acceso a Lunaris Ansenuza es: [0-9]{4}\\. Vence en 5 minutos\\."));
    }

    @Test
    void rejectsUnknownPassengerWithoutSendingOtp() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        when(passengers.findByPhone("3515555555")).thenReturn(Optional.empty());

        PassengerOtpService service = new PassengerOtpService(
                passengers, messaging, Duration.ofMinutes(5), Duration.ofHours(12));

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> service.sendOtp("3515555555"));

        assertEquals("No existe un pasajero registrado con ese teléfono.", exception.getMessage());
    }
}
