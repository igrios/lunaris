package com.lunaris.ansenuza.service.interurban;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class MercadoPagoWebhookController {
    private final MercadoPagoWebhookService service;

    public MercadoPagoWebhookController(MercadoPagoWebhookService service) { this.service = service; }

    @PostMapping("/webhook/interurban/mercadopago")
    public ResponseEntity<Void> receive(@RequestParam("data.id") String paymentId,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestBody MercadoPagoWebhookService.Notification body) {
        service.receive(paymentId, requestId, signature, body);
        return ResponseEntity.ok().build();
    }
}
