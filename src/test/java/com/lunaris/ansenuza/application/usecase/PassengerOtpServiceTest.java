package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        verify(messaging).sendOtp(
                eq("543515555555"), eq("Ana Pérez"), matches("[0-9]{4}"));
    }

    @Test
    void verifiesOtpUsingSameNationalKeyForDifferentPhoneFormats() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Passenger passenger = Passenger.builder()
                .firstName("Ana")
                .lastName("Pérez")
                .phone("543512282251")
                .build();
        when(passengers.findByPhone("543512282251")).thenReturn(Optional.of(passenger));

        PassengerOtpService service = new PassengerOtpService(
                passengers, messaging, Duration.ofMinutes(10), Duration.ofHours(12));

        service.sendOtp("+54 9 351-2282251");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(messaging).sendOtp(eq("543512282251"), eq("Ana Pérez"), codeCaptor.capture());
        String code = codeCaptor.getValue();

        PassengerOtpService.TokenResult result = service.verifyOtp("351 228-2251", code);

        assertNotNull(result.accessToken());
        assertEquals(Optional.of("543512282251"), service.resolvePhone(result.accessToken()));
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
        verify(messaging).sendOtp(
                eq("543515555555"), eq("Juan Pérez"), matches("[0-9]{4}"));
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

        verify(messaging).sendOtp(
                eq("543515555555"), eq("Juan Pérez"), matches("[0-9]{4}"));
    }
}
