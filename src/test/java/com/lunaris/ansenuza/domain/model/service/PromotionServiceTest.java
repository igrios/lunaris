package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.lunaris.ansenuza.domain.exception.PromotionExpiredException;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.PromotionUsage;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;
import com.lunaris.ansenuza.domain.repository.PromotionUsageRepository;

class PromotionServiceTest {

    @Test
    void consumesIndividualPromotionGlobally() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(false, null);
        when(fixtures.promotions.findByCodeForUpdate("1234")).thenReturn(Optional.of(promotion));

        fixtures.service.consume("1234", "+54 9 3564 00-0000");

        assertTrue(promotion.isUsed());
        verify(fixtures.promotions).saveAndFlush(promotion);
        ArgumentCaptor<PromotionUsage> usage = ArgumentCaptor.forClass(PromotionUsage.class);
        verify(fixtures.usages).saveAndFlush(usage.capture());
        assertEquals("543564000000", usage.getValue().getPhoneNumber());
    }

    @Test
    void massivePromotionCreatesOneUsageForNormalizedPhone() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findByCodeForUpdate("1234")).thenReturn(Optional.of(promotion));

        fixtures.service.consume("1234", "+54 9 351 228-2251");

        ArgumentCaptor<PromotionUsage> captor = ArgumentCaptor.forClass(PromotionUsage.class);
        verify(fixtures.usages).countByPromotionAndNormalizedPhone(
                promotion.getId(), "543512282251");
        verify(fixtures.usages).saveAndFlush(captor.capture());
        assertEquals("543512282251", captor.getValue().getPhoneNumber());
    }

    @Test
    void rejectsMassivePromotionAlreadyUsedByPhone() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));
        when(fixtures.usages.countByPromotionAndNormalizedPhone(promotion.getId(), "543512282251"))
                .thenReturn(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fixtures.service.requireAvailable("1234", "+54 351 228-2251"));

        assertEquals("Ya has utilizado este código", exception.getMessage());
    }

    @Test
    void rejectsExpiredMassivePromotion() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().minusMinutes(1));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));

        assertThrows(PromotionExpiredException.class,
                () -> fixtures.service.requireAvailable("1234", "5493564000000"));
    }

    @Test
    void createsMassivePromotionWithRequestedDuration() {
        Fixtures fixtures = fixtures();
        when(fixtures.promotions.saveAndFlush(org.mockito.ArgumentMatchers.any(Promotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime before = LocalDateTime.now().plusDays(3);

        Promotion promotion = fixtures.service.createMassive(50, Duration.ofDays(3));

        LocalDateTime after = LocalDateTime.now().plusDays(3);
        assertTrue(promotion.isMassive());
        assertTrue(!promotion.getExpiresAt().isBefore(before));
        assertTrue(!promotion.getExpiresAt().isAfter(after));
    }

    private Fixtures fixtures() {
        PromotionRepository promotions = mock(PromotionRepository.class);
        PromotionUsageRepository usages = mock(PromotionUsageRepository.class);
        return new Fixtures(promotions, usages, new PromotionService(promotions, usages));
    }

    private Promotion promotion(boolean massive, LocalDateTime expiresAt) {
        Promotion promotion = new Promotion();
        promotion.setId(UUID.randomUUID());
        promotion.setCode("1234");
        promotion.setMassive(massive);
        promotion.setExpiresAt(expiresAt);
        return promotion;
    }

    private record Fixtures(PromotionRepository promotions, PromotionUsageRepository usages,
            PromotionService service) {}
}
