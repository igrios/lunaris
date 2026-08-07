package com.lunaris.ansenuza.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.BusinessParameter;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DataInitializerTest {

    @Test
    void seedsLocalitiesFaresAndEssentialConfigurationWhenDatabaseIsEmpty() {
        LocalityRepository localities = mock(LocalityRepository.class);
        FareRepository fares = mock(FareRepository.class);
        BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
        SystemConfigurationRepository configurations =
                mock(SystemConfigurationRepository.class);
        when(localities.count()).thenReturn(0L);
        when(fares.count()).thenReturn(0L);

        new DataInitializer(localities, fares, parameters, configurations).run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Locality>> localityCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Fare>> fareCaptor = ArgumentCaptor.forClass(List.class);
        verify(localities).saveAllAndFlush(localityCaptor.capture());
        verify(fares).saveAllAndFlush(fareCaptor.capture());

        List<Locality> seededLocalities = localityCaptor.getValue();
        List<Fare> seededFares = fareCaptor.getValue();
        assertEquals(9, seededLocalities.size());
        assertEquals(9, seededFares.size());
        assertTrue(seededLocalities.stream().allMatch(locality -> locality.getId() != null));
        assertTrue(seededFares.stream().allMatch(fare -> fare.getId() != null));

        Set<String> localityNames = seededLocalities.stream()
                .map(Locality::getName)
                .collect(Collectors.toSet());
        Set<String> fareLocalities = seededFares.stream()
                .map(Fare::getLocalityName)
                .collect(Collectors.toSet());
        assertEquals(localityNames, fareLocalities);
        assertTrue(localityNames.containsAll(Set.of(
                "Córdoba Capital", "Miramar", "Balnearia", "La Para",
                "Obispo Trejo", "Villa Santa Rosa", "Marull", "La Puerta",
                "Río Primero")));

        verify(parameters, times(2)).save(any(BusinessParameter.class));
        verify(configurations, times(10)).save(any(SystemConfiguration.class));
    }

    @Test
    void doesNotDuplicateRouteDataOrOverwriteExistingConfiguration() {
        LocalityRepository localities = mock(LocalityRepository.class);
        FareRepository fares = mock(FareRepository.class);
        BusinessParameterRepository parameters = mock(BusinessParameterRepository.class);
        SystemConfigurationRepository configurations =
                mock(SystemConfigurationRepository.class);
        when(localities.count()).thenReturn(1L);
        when(fares.count()).thenReturn(1L);
        when(parameters.existsById(any())).thenReturn(true);
        when(configurations.existsById(any())).thenReturn(true);

        new DataInitializer(localities, fares, parameters, configurations).run();

        verify(localities, never()).saveAllAndFlush(any());
        verify(fares, never()).saveAllAndFlush(any());
        verify(parameters, never()).save(any());
        verify(configurations, never()).save(any());
    }
}
