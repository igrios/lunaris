package com.lunaris.ansenuza.application.usecase;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessPromotionCommandUseCase {

    private static final Pattern PROMOTION_COMMAND = Pattern.compile("^PROMO\\s+(GRATIS|\\d{1,3})$", Pattern.CASE_INSENSITIVE);

    private final PromotionService promotionService;
    private final MessagingPort messaging;

    @Value("${promotions.authorized-operator-phone:}")
    private String martinPhoneNumber;

    public boolean isPromotionCommand(String body) {
        return body != null && body.trim().toUpperCase(Locale.ROOT).startsWith("PROMO");
    }

    public void execute(String phoneNumber, String body) {
        if (!normalize(phoneNumber).equals(normalize(martinPhoneNumber))) {
            messaging.sendText(phoneNumber, "⛔ No estás autorizado para generar promociones.");
            return;
        }

        Matcher matcher = PROMOTION_COMMAND.matcher(body.trim());
        if (!matcher.matches()) {
            messaging.sendText(phoneNumber, "Formato inválido. Usá: *PROMO 10* a *PROMO 100* o *PROMO GRATIS*.");
            return;
        }

        int percentage = "GRATIS".equalsIgnoreCase(matcher.group(1)) ? 100 : Integer.parseInt(matcher.group(1));
        try {
            Promotion promotion = promotionService.create(percentage);
            messaging.sendText(phoneNumber, "✅ Promoción creada: código *%s* con *%d%%* de descuento."
                    .formatted(promotion.getCode(), promotion.getDiscountPercentage()));
        } catch (IllegalArgumentException exception) {
            messaging.sendText(phoneNumber, "❌ " + exception.getMessage());
        }
    }

    private String normalize(String number) {
        return number == null ? "" : number.replaceAll("[^0-9]", "");
    }
}
