package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.FareLocalityInUseException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteFareLocalityServiceTest {
    private FareRepository fareRepository;
    private LocalityRepository localityRepository;
    private ReservationRepository reservationRepository;
    private DeleteFareLocalityService service;
    private Fare fare;
    private Locality locality;

    @BeforeEach
    void setUp() {
        fareRepository = mock(FareRepository.class);
        localityRepository = mock(LocalityRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        service = new DeleteFareLocalityService(fareRepository, localityRepository, reservationRepository);
        fare = Fare.builder().id(UUID.randomUUID()).localityName("Miramar").amount(BigDecimal.TEN).build();
        locality = Locality.builder().id(UUID.randomUUID()).name("Miramar").build();
        when(fareRepository.findById(fare.getId())).thenReturn(Optional.of(fare));
        when(localityRepository.findFirstByNameIgnoreCase("Miramar")).thenReturn(Optional.of(locality));
    }

    @Test
    void deletesFareAndLocalityWhenUnused() {
        when(reservationRepository.existsActiveByLocality("Miramar")).thenReturn(false);

        service.delete(fare.getId());

        verify(fareRepository).delete(fare);
        verify(localityRepository).delete(locality);
    }

    @Test
    void keepsBothRecordsWhenAnActiveReservationDependsOnThem() {
        when(reservationRepository.existsActiveByLocality("Miramar")).thenReturn(true);

        assertThatThrownBy(() -> service.delete(fare.getId()))
                .isInstanceOf(FareLocalityInUseException.class);
        verify(fareRepository, never()).delete(fare);
        verify(localityRepository, never()).delete(locality);
    }
}
