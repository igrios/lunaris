package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.lunaris.ansenuza.domain.exception.PromotionExpiredException;
import com.lunaris.ansenuza.domain.exception.MassivePromotionAlreadyUsedException;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.PromotionUsage;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;
import com.lunaris.ansenuza.domain.repository.PromotionUsageRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

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
    void consumesMassivePromotionAfterItsOwnReservationWasPersisted() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findByCodeForUpdate("1234")).thenReturn(Optional.of(promotion));
        when(fixtures.reservations.existsActivePromotionUsageByPhone(
                "543511111111", promotion.getId(), "1234")).thenReturn(true);

        fixtures.service.consume("1234", "3511111111");

        verify(fixtures.usages).saveAndFlush(
                org.mockito.ArgumentMatchers.any(PromotionUsage.class));
    }

    @Test
    void addsArgentinaCountryCodeToTenDigitLocalPhone() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findByCodeForUpdate("1234")).thenReturn(Optional.of(promotion));

        fixtures.service.consume("1234", "351 228-2251");

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

        MassivePromotionAlreadyUsedException exception = assertThrows(
                MassivePromotionAlreadyUsedException.class,
                () -> fixtures.service.requireAvailable("1234", "+54 351 228-2251"));

        assertEquals("Esta promoción masiva ya fue utilizada por este número de teléfono",
                exception.getMessage());
    }

    @Test
    void allowsFirstMassivePromotionReservationForPhoneA() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));

        assertEquals(promotion,
                fixtures.service.requireAvailable("1234", "+54 9 351 111-1111"));

        verify(fixtures.reservations).existsActivePromotionUsageByPhone(
                "543511111111", promotion.getId(), "1234");
    }

    @Test
    void rejectsSecondActiveReservationWithSameMassivePromotionForPhoneA() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));
        when(fixtures.reservations.existsActivePromotionUsageByPhone(
                "543511111111", promotion.getId(), "1234")).thenReturn(true);

        MassivePromotionAlreadyUsedException exception = assertThrows(
                MassivePromotionAlreadyUsedException.class,
                () -> fixtures.service.requireAvailable("1234", "3511111111"));

        assertEquals("Esta promoción masiva ya fue utilizada por este número de teléfono",
                exception.getMessage());
    }

    @Test
    void allowsDifferentPhoneBToUseSameMassivePromotion() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(true, LocalDateTime.now().plusDays(2));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));
        when(fixtures.reservations.existsActivePromotionUsageByPhone(
                "543511111111", promotion.getId(), "1234")).thenReturn(true);

        assertEquals(promotion,
                fixtures.service.requireAvailable("1234", "3512222222"));
        verify(fixtures.reservations).existsActivePromotionUsageByPhone(
                "543512222222", promotion.getId(), "1234");
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
    void rejectsExpiredIndividualPromotion() {
        Fixtures fixtures = fixtures();
        Promotion promotion = promotion(false, LocalDateTime.now().minusMinutes(1));
        when(fixtures.promotions.findFirstByCode("1234")).thenReturn(Optional.of(promotion));

        assertThrows(PromotionExpiredException.class,
                () -> fixtures.service.requireAvailable("1234", "5493564000000"));
    }

    @Test
    void calculatesDiscountOnlyOnPositiveBaseAndNeverAboveIt() {
        Fixtures fixtures = fixtures();

        assertEquals(new BigDecimal("2500.00"),
                fixtures.service.calculateDiscount(new BigDecimal("10000.00"), 25));
        assertEquals(new BigDecimal("0.00"),
                fixtures.service.calculateDiscount(new BigDecimal("-100.00"), 25));
        assertEquals(new BigDecimal("10000.00"),
                fixtures.service.calculateDiscount(new BigDecimal("10000.00"), 100));
        assertThrows(IllegalArgumentException.class,
                () -> fixtures.service.calculateDiscount(new BigDecimal("10000.00"), 101));
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
        ReservationRepository reservations = mock(ReservationRepository.class);
        return new Fixtures(promotions, usages, reservations,
                new PromotionService(promotions, usages, reservations));
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
            ReservationRepository reservations, PromotionService service) {}
}
