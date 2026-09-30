package com.lunaris.ansenuza.service.interurban;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MercadoPagoWebhookController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanPaymentExceptionHandler {
    @ExceptionHandler(InterurbanPaymentException.class)
    public ResponseEntity<Map<String, String>> domain(InterurbanPaymentException exception) {
        int status = switch (exception.code()) {
            case INVALID_SIGNATURE -> 401;
            case INVALID_NOTIFICATION -> 400;
            case PROVIDER_UNAVAILABLE, QR_FAILURE -> 503;
        };
        return ResponseEntity.status(status).body(Map.of("code", exception.code().name()));
    }

    @ExceptionHandler({DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public ResponseEntity<Map<String, String>> storage(Exception exception) {
        return ResponseEntity.status(503).body(Map.of("code", "RETRY_REQUIRED"));
    }
}
