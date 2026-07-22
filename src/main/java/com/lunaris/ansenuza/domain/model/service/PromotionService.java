package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Transactional
    public Promotion create(int discountPercentage) {
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
        return promotionRepository.saveAndFlush(promotion);
    }

    @Transactional(readOnly = true)
    public Promotion requireAvailable(String code) {
        return promotionRepository.findFirstByCode(code)
                .filter(promotion -> !promotion.isUsed())
                .orElseThrow(() -> new IllegalArgumentException("El código promocional no existe o ya fue utilizado."));
    }

    public BigDecimal calculateDiscount(BigDecimal total, int discountPercentage) {
        return total.multiply(BigDecimal.valueOf(discountPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void consume(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        Promotion promotion = promotionRepository.findFirstByCodeAndUsedFalse(code)
                .orElseThrow(() -> new IllegalStateException("La promoción ya fue utilizada o no existe."));
        promotion.setUsed(true);
        promotionRepository.saveAndFlush(promotion);
    }

    @Transactional
    public void consumeIfAvailable(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        promotionRepository.findFirstByCodeAndUsedFalse(code).ifPresent(promotion -> {
            promotion.setUsed(true);
            promotionRepository.saveAndFlush(promotion);
        });
    }
}
