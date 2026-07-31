package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.mockito.ArgumentCaptor;
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

        verify(passengers, never()).save(any(Passenger.class));
        verify(messaging).sendText(
                eq("543515555555"),
                matches("Tu código de acceso a Lunaris Ansenuza es: [0-9]{4}\\. Vence en 5 minutos\\."));
    }

    @Test
    void createsUnknownPassengerAndSendsOtp() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        when(passengers.findByPhone("543515555555")).thenReturn(Optional.empty());
        when(passengers.findByPhone("3515555555")).thenReturn(Optional.empty());
        when(passengers.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassengerOtpService service = new PassengerOtpService(
                passengers, messaging, Duration.ofMinutes(5), Duration.ofHours(12));

        service.sendOtp("3515555555", "Juan Pérez");

        ArgumentCaptor<Passenger> passengerCaptor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengers).save(passengerCaptor.capture());
        assertEquals("Juan", passengerCaptor.getValue().getFirstName());
        assertEquals("Pérez", passengerCaptor.getValue().getLastName());
        assertEquals("543515555555", passengerCaptor.getValue().getPhone());
        verify(messaging).sendText(
                eq("543515555555"),
                matches("Tu código de acceso a Lunaris Ansenuza es: [0-9]{4}\\. Vence en 5 minutos\\."));
    }

    @Test
    void resolvesPassengerCreatedByConcurrentRequestAfterOptimisticLockFailure() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Passenger concurrentlyCreated = Passenger.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .phone("543515555555")
                .build();
        when(passengers.findByPhone("543515555555"))
                .thenReturn(Optional.empty(), Optional.of(concurrentlyCreated));
        when(passengers.findByPhone("3515555555")).thenReturn(Optional.empty());
        when(passengers.save(any(Passenger.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Passenger.class, "concurrent"));

        PassengerOtpService service = new PassengerOtpService(
                passengers, messaging, Duration.ofMinutes(5), Duration.ofHours(12));

        service.sendOtp("3515555555", "Juan Pérez");

        verify(messaging).sendText(
                eq("543515555555"),
                matches("Tu código de acceso a Lunaris Ansenuza es: [0-9]{4}\\. Vence en 5 minutos\\."));
    }
}
