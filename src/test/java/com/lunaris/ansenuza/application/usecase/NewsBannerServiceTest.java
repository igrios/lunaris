package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import com.lunaris.ansenuza.domain.repository.NewsBannerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class NewsBannerServiceTest {

    @Test
    void createsBannerWithNullIdSoHibernatePerformsInsert() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        NewsBannerStoragePort storage = mock(NewsBannerStoragePort.class);
        MockMultipartFile image = new MockMultipartFile(
                "image", "flyer.jpg", "image/jpeg", "image".getBytes());
        when(storage.upload(image)).thenReturn("https://res.cloudinary.com/flyer.jpg");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NewsBannerService service = new NewsBannerService(repository, storage);

        NewsBanner result = service.create(
                "  Promo agosto  ", true, LocalDate.of(2026, 8, 31), image);

        assertNull(result.getId());
        assertEquals("Promo agosto", result.getTitle());
        assertEquals("https://res.cloudinary.com/flyer.jpg", result.getImageUrl());
        assertEquals("PROMO_AGOSTO", result.getEventType());
        verify(repository).save(result);
    }

    @Test
    void updatesFetchedBannerAndKeepsCurrentImageWhenNoReplacementArrives() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        NewsBannerStoragePort storage = mock(NewsBannerStoragePort.class);
        UUID id = UUID.randomUUID();
        NewsBanner existing = new NewsBanner();
        existing.setId(id);
        existing.setImageUrl("https://example.com/original.jpg");
        when(repository.findById(id)).thenReturn(java.util.Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        NewsBannerService service = new NewsBannerService(repository, storage);

        NewsBanner result = service.save(id, "Título actualizado", "Descripción",
                "evento_actualizado", true, false, LocalDate.of(2026, 12, 1),
                null, null);

        assertEquals(id, result.getId());
        assertEquals("Título actualizado", result.getTitle());
        assertEquals("EVENTO_ACTUALIZADO", result.getEventType());
        assertEquals("https://example.com/original.jpg", result.getImageUrl());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void createsWaitingListBannerFromExternalUrlAndExplicitEventType() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NewsBannerService service = new NewsBannerService(
                repository, mock(NewsBannerStoragePort.class));

        NewsBanner result = service.create("Airbag - Estadio Belgrano",
                "10 de Octubre · Traslados al show", "airbag_cordoba", true, true,
                null, "https://example.com/airbag.jpg", null);

        assertEquals("AIRBAG_CORDOBA", result.getEventType());
        assertEquals("10 de Octubre · Traslados al show", result.getDescription());
        assertEquals("https://example.com/airbag.jpg", result.getImageUrl());
        assertEquals(true, result.isHasWaitingList());
    }

    @Test
    void requestsOnlyCurrentlyActiveBanners() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        when(repository.findActiveOn(any(LocalDate.class))).thenReturn(List.of(new NewsBanner()));
        NewsBannerService service = new NewsBannerService(
                repository, mock(NewsBannerStoragePort.class));

        assertEquals(1, service.findActive().size());
        verify(repository).findActiveOn(any(LocalDate.class));
    }

    @Test
    void deletesExistingBanner() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        NewsBannerService service = new NewsBannerService(
                repository, mock(NewsBannerStoragePort.class));

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
