package com.lunaris.ansenuza.service.interurban;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import org.springframework.web.client.RestClient;

public class MercadoPagoPaymentClient implements PaymentQueryPort {
    private final RestClient client;

    public MercadoPagoPaymentClient(RestClient client) { this.client = client; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(String id, String status,
            @JsonProperty("external_reference") String reference,
            @JsonProperty("transaction_amount") BigDecimal amount,
            @JsonProperty("currency_id") String currency,
            @JsonProperty("collector_id") String collector,
            @JsonProperty("live_mode") Boolean liveMode,
            @JsonProperty("transaction_amount_refunded") BigDecimal refunded) {}

    @Override public Payment getPayment(String paymentId) {
        if (paymentId == null || !paymentId.matches("[0-9]{1,64}")) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_NOTIFICATION);
        }
        try {
            Response response = client.get().uri("/v1/payments/{id}", paymentId).retrieve().body(Response.class);
            if (response == null || response.id() == null || response.status() == null || response.amount() == null
                    || response.currency() == null || response.collector() == null || response.liveMode() == null
                    || response.refunded() == null) {
                throw new InterurbanPaymentException(InterurbanPaymentException.Code.PROVIDER_UNAVAILABLE);
            }
            return new Payment(response.id(), response.status(), response.reference(), response.amount(),
                    response.currency(), response.collector(), response.liveMode(), response.refunded());
        } catch (RuntimeException e) {
            // No propagar response body ni cabeceras de autorización hacia logs/HTTP.
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.PROVIDER_UNAVAILABLE);
        }
    }
}
