package com.lunaris.ansenuza.application.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.service.PromotionService;

class ProcessPromotionCommandUseCaseTest {

    @Test
    void createsPromotionWithDurationInDaysAndReportsExpiration() {
        PromotionService promotionService = mock(PromotionService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Promotion promotion = promotion("1234", 10, LocalDateTime.of(2026, 7, 25, 14, 30));
        when(promotionService.createMassive(10, Duration.ofDays(3))).thenReturn(promotion);
        ProcessPromotionCommandUseCase useCase = useCase(promotionService, messaging);

        useCase.execute("5493564000000", "PROMO MASIVA 10 3D");

        verify(promotionService).createMassive(10, Duration.ofDays(3));
        verify(messaging).sendText("5493564000000",
                "✅ Promoción masiva creada: código *1234* con *10%* de descuento. Vence el *25/07/2026 14:30 hs*.");
    }

    @Test
    void createsFreeIndividualPromotion() {
        PromotionService promotionService = mock(PromotionService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Promotion promotion = promotion("5678", 100, null);
        when(promotionService.create(100)).thenReturn(promotion);
        ProcessPromotionCommandUseCase useCase = useCase(promotionService, messaging);

        useCase.execute("5493564000000", "PROMO GRATIS");

        verify(promotionService).create(100);
    }

    @Test
    void massivePromotionDefaultsToSevenDays() {
        PromotionService promotionService = mock(PromotionService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        Promotion promotion = promotion("9012", 50, LocalDateTime.of(2026, 7, 29, 10, 0));
        when(promotionService.createMassive(50, Duration.ofDays(7))).thenReturn(promotion);
        ProcessPromotionCommandUseCase useCase = useCase(promotionService, messaging);

        useCase.execute("5493564000000", "PROMO MASIVA 50");

        verify(promotionService).createMassive(50, Duration.ofDays(7));
    }

    private ProcessPromotionCommandUseCase useCase(PromotionService service, MessagingPort messaging) {
        ProcessPromotionCommandUseCase useCase = new ProcessPromotionCommandUseCase(service, messaging);
        ReflectionTestUtils.setField(useCase, "martinPhoneNumber", "5493564000000");
        return useCase;
    }

    private Promotion promotion(String code, int percentage, LocalDateTime expiration) {
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setDiscountPercentage(percentage);
        promotion.setExpiresAt(expiration);
        return promotion;
    }
}
