package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentWebhookUseCase;
import com.lunaris.ansenuza.infrastructure.adapters.payment.mercadopago.MercadoPagoWebhookResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mercadopago")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
    private final MercadoPagoWebhookResolver webhookResolver;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody(required = false) JsonNode payload) {
        try {
            processPaymentWebhookUseCase.process(webhookResolver.resolve(payload));
        } catch (Exception exception) {
            log.error("No se pudo procesar el webhook de Mercado Pago.", exception);
        }
        return ResponseEntity.ok().build();
    }

}
