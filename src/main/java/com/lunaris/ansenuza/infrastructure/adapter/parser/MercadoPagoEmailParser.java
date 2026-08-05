package com.lunaris.ansenuza.infrastructure.adapter.parser;

import com.lunaris.ansenuza.application.payment.BankTransferNotification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoEmailParser {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern TRANSACTION_ID = Pattern.compile(
            "(?:n[uú]mero\\s+de\\s+operaci[oó]n|id\\s+de\\s+transacci[oó]n|"
                    + "operaci[oó]n|transaction\\s+id)\\s*[:#]?\\s*([A-Z0-9-]{5,120})", FLAGS);
    private static final Pattern AMOUNT = Pattern.compile(
            "(?:monto|importe|amount|recibiste)\\s*[:$]?\\s*(?:ARS\\s*)?\\$?\\s*"
                    + "([0-9][0-9.,]*[0-9]|[0-9])", FLAGS);
    private static final Pattern PAYER = Pattern.compile(
            "(?:pagador|payer|enviado\\s+por|recibiste\\s+(?:un\\s+)?pago\\s+de)"
                    + "\\s*:?\\s*([\\p{L}][\\p{L} .'’-]{1,178})", FLAGS);
    private static final Pattern RESERVATION_CODE = Pattern.compile(
            "(?:reserva|c[oó]digo\\s+de\\s+reserva|referencia)\\s*[:#]?\\s*"
                    + "([A-Z]{2,5}-[A-Z]{2,5}-[0-9]{3}(?:-IDA|-VUELTA)?)", FLAGS);

    public Optional<BankTransferNotification> parse(
            String externalMessageId,
            String subject,
            String body,
            Instant receivedAt) {
        String content = String.join("\n", nullSafe(subject), nullSafe(body));
        Optional<String> transactionId = capture(TRANSACTION_ID, content);
        Optional<String> rawAmount = capture(AMOUNT, content);
        Optional<String> payerName = capture(PAYER, content).map(this::firstLine);
        Optional<String> reservationCode = capture(RESERVATION_CODE, content);

        if (transactionId.isEmpty() || rawAmount.isEmpty()
                || payerName.isEmpty() || reservationCode.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new BankTransferNotification(
                    "MERCADO_PAGO_EMAIL",
                    externalMessageId,
                    transactionId.get().toUpperCase(Locale.ROOT),
                    reservationCode.get().toUpperCase(Locale.ROOT),
                    parseArgentineAmount(rawAmount.get()),
                    payerName.get(),
                    receivedAt));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> capture(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
    }

    private BigDecimal parseArgentineAmount(String value) {
        String normalized = value.replace(" ", "");
        int lastComma = normalized.lastIndexOf(',');
        int lastDot = normalized.lastIndexOf('.');
        if (lastComma > lastDot) {
            normalized = normalized.replace(".", "").replace(',', '.');
        } else if (lastDot >= 0) {
            normalized = normalized.replace(",", "");
        }
        return new BigDecimal(normalized);
    }

    private String firstLine(String value) {
        return value.lines().findFirst().orElse(value).trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
