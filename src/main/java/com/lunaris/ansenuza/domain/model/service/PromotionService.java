package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.PromotionUsage;
import com.lunaris.ansenuza.domain.exception.PromotionExpiredException;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;
import com.lunaris.ansenuza.domain.repository.PromotionUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    @Transactional
    public Promotion create(int discountPercentage) {
        return createPromotion(discountPercentage, false, null);
    }

    @Transactional
    public Promotion createMassive(int discountPercentage, Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("La duración de la promoción debe ser mayor a cero.");
        }
        return createPromotion(discountPercentage, true, LocalDateTime.now().plus(duration));
    }

    private Promotion createPromotion(int discountPercentage, boolean massive, LocalDateTime expiresAt) {
        if (discountPercentage < 10 || discountPercentage > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 10% y 100%.");
        }
        String code;
        do {
            code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10_000));
        } while (promotionRepository.existsByCode(code));

        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setDiscountPercentage(discountPercentage);
        promotion.setUsed(false);
        promotion.setMassive(massive);
        promotion.setExpiresAt(expiresAt);
        return promotionRepository.saveAndFlush(promotion);
    }

    @Transactional(readOnly = true)
    public Promotion requireAvailable(String code, String phoneNumber) {
        Promotion promotion = promotionRepository.findFirstByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("El código promocional no existe."));
        validateAvailable(promotion, phoneNumber);
        return promotion;
    }

    public BigDecimal calculateDiscount(BigDecimal total, int discountPercentage) {
        return total.multiply(BigDecimal.valueOf(discountPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void consume(String code, String phoneNumber) {
        if (code == null || code.isBlank()) {
            return;
        }
        Promotion promotion = promotionRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new IllegalStateException("La promoción no existe."));
        validateAvailable(promotion, phoneNumber);
        if (promotion.isMassive()) {
            promotionUsageRepository.saveAndFlush(new PromotionUsage(promotion, normalizePhone(phoneNumber)));
        } else {
            promotion.setUsed(true);
            promotionRepository.saveAndFlush(promotion);
        }
    }

    @Transactional
    public void consumeIfAvailable(String code, String phoneNumber) {
        if (code == null || code.isBlank()) {
            return;
        }
        promotionRepository.findByCodeForUpdate(code).ifPresent(promotion -> {
            String normalizedPhone = normalizePhone(phoneNumber);
            if (promotion.isMassive()) {
                validateNotExpired(promotion);
                if (!promotionUsageRepository.existsByPromotionIdAndPhoneNumber(promotion.getId(), normalizedPhone)) {
                    promotionUsageRepository.saveAndFlush(new PromotionUsage(promotion, normalizedPhone));
                }
            } else if (!promotion.isUsed()) {
                promotion.setUsed(true);
                promotionRepository.saveAndFlush(promotion);
            }
        });
    }

    private void validateAvailable(Promotion promotion, String phoneNumber) {
        if (!promotion.isMassive()) {
            if (promotion.isUsed()) {
                throw new IllegalArgumentException("El código promocional ya fue utilizado.");
            }
            return;
        }
        validateNotExpired(promotion);
        String normalizedPhone = normalizePhone(phoneNumber);
        if (normalizedPhone.isBlank()) {
            throw new IllegalArgumentException("No se pudo identificar el teléfono para aplicar la promoción.");
        }
        if (promotionUsageRepository.existsByPromotionIdAndPhoneNumber(promotion.getId(), normalizedPhone)) {
            throw new IllegalArgumentException("Este teléfono ya utilizó el código promocional.");
        }
    }

    private void validateNotExpired(Promotion promotion) {
        if (promotion.getExpiresAt() == null || LocalDateTime.now().isAfter(promotion.getExpiresAt())) {
            throw new PromotionExpiredException();
        }
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.replaceAll("[^0-9]", "");
    }
}
