package com.lunaris.ansenuza.infrastructure.adapters.payment.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.payment.PaymentPreference;
import com.lunaris.ansenuza.domain.model.service.PaymentFeeCalculator;
import com.lunaris.ansenuza.domain.port.outbound.PaymentGatewayPort;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoAdapter implements PaymentGatewayPort {

    private static final String WEBHOOK_PATH = "/api/v1/mercadopago/webhook";

    private final String accessToken;
    private final String serverBaseUrl;
    private final PaymentFeeCalculator feeCalculator;

    public MercadoPagoAdapter(
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${mercadopago.server-base-url}") String serverBaseUrl,
            PaymentFeeCalculator feeCalculator) {
        this.accessToken = accessToken;
        this.serverBaseUrl = stripTrailingSlash(serverBaseUrl);
        this.feeCalculator = feeCalculator;
    }

    @Override
    public PaymentPreference createPaymentPreference(
            Reservation reservation, BigDecimal baseAmount) {
        validate(reservation, baseAmount);
        BigDecimal finalAmount = feeCalculator.calculateFinalAmount(baseAmount);
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(reservation.getReservationCode())
                    .title("Reserva Lunaris " + reservation.getReservationCode())
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(finalAmount)
                    .build();
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference(reservation.getReservationCode())
                    .notificationUrl(serverBaseUrl + WEBHOOK_PATH)
                    .build();
            Preference preference = new PreferenceClient().create(request);
            return new PaymentPreference(
                    preference.getId(),
                    preference.getInitPoint(),
                    reservation.getReservationCode(),
                    baseAmount,
                    finalAmount);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el link de Mercado Pago.", exception);
        }
    }

    private void validate(Reservation reservation, BigDecimal baseAmount) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("MERCADOPAGO_ACCESS_TOKEN no está configurado.");
        }
        if (reservation == null
                || reservation.getReservationCode() == null
                || reservation.getReservationCode().isBlank()) {
            throw new IllegalArgumentException("La reserva debe tener código.");
        }
        if (baseAmount == null || baseAmount.signum() <= 0) {
            throw new IllegalArgumentException("El monto a cobrar debe ser positivo.");
        }
    }

    private static String stripTrailingSlash(String value) {
        return value != null && value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}
