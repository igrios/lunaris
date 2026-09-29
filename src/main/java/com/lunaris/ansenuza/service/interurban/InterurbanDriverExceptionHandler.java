package com.lunaris.ansenuza.service.interurban;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = InterurbanDriverController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.driver.enabled"}, havingValue = "true")
public class InterurbanDriverExceptionHandler {
    @ExceptionHandler(QrExpiredException.class)
    public ResponseEntity<Map<String, String>> expired() { return error(410, "QR_EXPIRED"); }
    @ExceptionHandler(QrAlreadyConsumedException.class)
    public ResponseEntity<Map<String, String>> consumed() { return error(409, "QR_ALREADY_CONSUMED"); }
    @ExceptionHandler(InvalidTripException.class)
    public ResponseEntity<Map<String, String>> trip() { return error(409, "INVALID_TRIP"); }
    @ExceptionHandler(InvalidQrException.class)
    public ResponseEntity<Map<String, String>> qr() { return error(400, "INVALID_QR"); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> denied() { return error(403, "DRIVER_ACCESS_DENIED"); }
    @ExceptionHandler({org.springframework.dao.DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public ResponseEntity<Map<String, String>> storage() { return error(503, "CHECKIN_RETRY_REQUIRED"); }

    private ResponseEntity<Map<String, String>> error(int status, String code) {
        return ResponseEntity.status(status).cacheControl(org.springframework.http.CacheControl.noStore()).body(Map.of("code", code));
    }
}
