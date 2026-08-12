package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UpdatePassengerAddressUseCaseTest {

    @Test
    void reloadsAndUpdatesManagedPassenger() {
        PassengerRepository passengers = mock(PassengerRepository.class);
        Passenger managedPassenger = Passenger.builder()
                .phone("543512282251")
                .address("Dirección anterior")
                .locality("Porteña")
                .build();
        when(passengers.findByPhoneForUpdate("543512282251"))
                .thenReturn(Optional.of(managedPassenger));
        UpdatePassengerAddressUseCase useCase = new UpdatePassengerAddressUseCase(passengers);

        useCase.update("543512282251", "San Martín 450", "Morteros");

        assertEquals("San Martín 450", managedPassenger.getAddress());
        assertEquals("Morteros", managedPassenger.getLocality());
        verify(passengers, never()).saveAndFlush(managedPassenger);
    }
}
