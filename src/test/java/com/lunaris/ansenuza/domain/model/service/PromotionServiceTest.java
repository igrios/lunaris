package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;

class PromotionServiceTest {

    @Test
    void consumePersistsUsedPromotionImmediately() {
        PromotionRepository repository = mock(PromotionRepository.class);
        Promotion promotion = new Promotion();
        promotion.setCode("1234");
        when(repository.findFirstByCodeAndUsedFalse("1234")).thenReturn(Optional.of(promotion));

        new PromotionService(repository).consume("1234");

        assertTrue(promotion.isUsed());
        verify(repository).saveAndFlush(promotion);
    }
}
