package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateFareLocalityServiceTest {
    private LocalityRepository localityRepository;
    private FareRepository fareRepository;
    private CreateFareLocalityService service;

    @BeforeEach
    void setUp() {
        localityRepository = mock(LocalityRepository.class);
        fareRepository = mock(FareRepository.class);
        service = new CreateFareLocalityService(localityRepository, fareRepository);
    }

    @Test
    void createsLocalityAndFareTogether() {
        when(localityRepository.findFirstByNameIgnoreCase("Miramar")).thenReturn(Optional.empty());
        when(fareRepository.findFirstByLocalityNameIgnoreCase("Miramar")).thenReturn(Optional.empty());
        when(localityRepository.save(any(Locality.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fareRepository.save(any(Fare.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(" Miramar ", 197, 185, new BigDecimal("62000.00"));

        assertThat(result.localityName()).isEqualTo("Miramar");
        assertThat(result.fareId()).isNotNull();
        assertThat(result.localityId()).isNotNull();
        verify(localityRepository).save(any(Locality.class));
        verify(fareRepository).save(any(Fare.class));
    }

    @Test
    void rejectsDuplicateLocalityBeforeWriting() {
        when(localityRepository.findFirstByNameIgnoreCase("Miramar"))
                .thenReturn(Optional.of(Locality.builder().name("Miramar").build()));

        assertThatThrownBy(() -> service.create("Miramar", 197, 185, BigDecimal.TEN))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Ya existe una localidad");
        verify(localityRepository, never()).save(any());
        verify(fareRepository, never()).save(any());
    }
}
