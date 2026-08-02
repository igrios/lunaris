package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createsBannerWithCloudinaryUrlAndDefensiveUuid() {
        NewsBannerRepository repository = mock(NewsBannerRepository.class);
        NewsBannerStoragePort storage = mock(NewsBannerStoragePort.class);
        MockMultipartFile image = new MockMultipartFile(
                "image", "flyer.jpg", "image/jpeg", "image".getBytes());
        when(storage.upload(image)).thenReturn("https://res.cloudinary.com/flyer.jpg");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NewsBannerService service = new NewsBannerService(repository, storage);

        NewsBanner result = service.create(
                "  Promo agosto  ", true, LocalDate.of(2026, 8, 31), image);

        assertNotNull(result.getId());
        assertEquals("Promo agosto", result.getTitle());
        assertEquals("https://res.cloudinary.com/flyer.jpg", result.getImageUrl());
        verify(repository).save(result);
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
