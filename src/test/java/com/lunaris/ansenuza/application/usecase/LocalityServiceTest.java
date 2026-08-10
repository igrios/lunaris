package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalityServiceTest {

    @Test
    void returnsOnlyTheRepositoryActiveFareCatalog() {
        LocalityRepository repository = mock(LocalityRepository.class);
        Locality locality = Locality.builder().name("Morteros").build();
        when(repository.findAllWithActiveFare()).thenReturn(List.of(locality));

        List<Locality> result = new LocalityService(repository).findAllWithActiveFare();

        assertThat(result).containsExactly(locality);
        verify(repository).findAllWithActiveFare();
    }
}
