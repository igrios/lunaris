package com.lunaris.ansenuza.infrastructure.adapters.payment.mercadopago;

import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentWebhookUseCase.PaymentWebhookCommand;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
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
        String status = text(payload, "status");
        String externalReference = firstNonBlank(
                text(payload, "external_reference"),
                text(payload, "externalReference"),
                nestedText(payload, "data", "external_reference"));
        if (status != null && externalReference != null) {
            return new PaymentWebhookCommand(status, externalReference);
        }

        String paymentId = nestedText(payload, "data", "id");
        if (paymentId == null || paymentId.isBlank()) {
            return new PaymentWebhookCommand(status, externalReference);
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("MERCADOPAGO_ACCESS_TOKEN no está configurado.");
        }
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            Payment payment = new PaymentClient().get(Long.valueOf(paymentId));
            return new PaymentWebhookCommand(payment.getStatus(), payment.getExternalReference());
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudo consultar el pago " + paymentId + " en Mercado Pago.", exception);
        }
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
