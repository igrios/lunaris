package com.lunaris.ansenuza.infrastructure.adapters.payment.mercadopago;

import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentWebhookUseCase.PaymentWebhookCommand;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookResolver {

    private final String accessToken;

    public MercadoPagoWebhookResolver(
            @Value("${mercadopago.access-token:}") String accessToken) {
        this.accessToken = accessToken;
    }

    public PaymentWebhookCommand resolve(JsonNode payload) {
        if (payload == null) {
            return null;
        }
        String paymentId = firstNonBlank(text(payload, "id"), nestedText(payload, "data", "id"));
        String status = text(payload, "status");
        BigDecimal amount = decimal(payload, "transaction_amount");
        String externalReference = firstNonBlank(
                text(payload, "external_reference"),
                text(payload, "externalReference"),
                nestedText(payload, "data", "external_reference"));
        String payer = firstNonBlank(text(payload, "payer_email"), nestedText(payload, "payer", "email"));
        if (status != null && amount != null && paymentId != null) {
            return new PaymentWebhookCommand(paymentId, status, amount, externalReference, payer);
        }

        if (paymentId == null || paymentId.isBlank()) {
            return new PaymentWebhookCommand(null, status, amount, externalReference, payer);
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("MERCADOPAGO_ACCESS_TOKEN no está configurado.");
        }
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            Payment payment = new PaymentClient().get(Long.valueOf(paymentId));
            String payerIdentifier = payment.getPayer() == null ? null
                    : firstNonBlank(payment.getPayer().getEmail(), payment.getPayer().getId());
            return new PaymentWebhookCommand(paymentId, payment.getStatus(),
                    payment.getTransactionAmount(), payment.getExternalReference(), payerIdentifier);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el pago " + paymentId + " en Mercado Pago.", exception);
        }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.decimalValue();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String nestedText(JsonNode node, String parent, String field) {
        JsonNode nested = node.get(parent);
        return nested == null ? null : text(nested, field);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
