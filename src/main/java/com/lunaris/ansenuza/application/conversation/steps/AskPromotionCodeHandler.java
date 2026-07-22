package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AskPromotionCodeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PromotionService promotionService;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_PROMOTION_CODE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim();
        if ("SIN PROMO".equalsIgnoreCase(response) || "NO".equalsIgnoreCase(response)) {
            session.setPromotionCode(null);
            session.setPromotionDiscountPercentage(null);
        } else if (response.matches("\\d{4}")) {
            try {
                Promotion promotion = promotionService.requireAvailable(response, session.getPhoneNumber());
                session.setPromotionCode(promotion.getCode());
                session.setPromotionDiscountPercentage(promotion.getDiscountPercentage());
            } catch (IllegalArgumentException exception) {
                messaging.sendText(session.getPhoneNumber(), "❌ " + exception.getMessage()
                        + ". Ingresá otro código de 4 dígitos o escribí *SIN PROMO*.");
                return;
            }
        } else {
            messaging.sendText(session.getPhoneNumber(), "Ingresá un código promocional válido de 4 dígitos o escribí *SIN PROMO*.");
            return;
        }

        session.setCurrentStep("ASK_CONFIRMATION");
        conversationSessionRepository.saveAndFlush(session);
        presenter.sendReservationSummaryWithButtons(session.getPhoneNumber(), session);
    }
}
