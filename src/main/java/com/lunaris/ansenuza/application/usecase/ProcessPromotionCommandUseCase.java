package com.lunaris.ansenuza.application.usecase;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
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

    private static final Pattern INDIVIDUAL_COMMAND = Pattern.compile(
            "^PROMO\\s+(GRATIS|\\d{1,3})$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MASSIVE_COMMAND = Pattern.compile(
            "^PROMO\\s+MASIVA\\s+(GRATIS|\\d{1,3})(?:\\s+(\\d{1,6})D)?$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter EXPIRATION_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        Matcher massiveMatcher = MASSIVE_COMMAND.matcher(body.trim());
        Matcher individualMatcher = INDIVIDUAL_COMMAND.matcher(body.trim());
        boolean massive = massiveMatcher.matches();
        if (!massive && !individualMatcher.matches()) {
            messaging.sendText(phoneNumber,
                    "Formato inválido. Usá *PROMO 10*, *PROMO GRATIS* o *PROMO MASIVA 10 7D*.");
            return;
        }

        Matcher matcher = massive ? massiveMatcher : individualMatcher;
        int percentage = "GRATIS".equalsIgnoreCase(matcher.group(1)) ? 100 : Integer.parseInt(matcher.group(1));
        try {
            Promotion promotion = massive
                    ? promotionService.createMassive(percentage, parseDays(matcher.group(2)))
                    : promotionService.create(percentage);
            String response = massive
                    ? "✅ Promoción masiva creada: código *%s* con *%d%%* de descuento. Vence el *%s hs*."
                            .formatted(promotion.getCode(), promotion.getDiscountPercentage(),
                                    promotion.getExpiresAt().format(EXPIRATION_FORMAT))
                    : "✅ Promoción individual creada: código *%s* con *%d%%* de descuento."
                            .formatted(promotion.getCode(), promotion.getDiscountPercentage());
            messaging.sendText(phoneNumber, response);
        } catch (IllegalArgumentException exception) {
            messaging.sendText(phoneNumber, "❌ " + exception.getMessage());
        }
    }

    private Duration parseDays(String amount) {
        return Duration.ofDays(amount == null ? 7 : Long.parseLong(amount));
    }

    private String normalize(String number) {
        return number == null ? "" : number.replaceAll("[^0-9]", "");
    }
}
