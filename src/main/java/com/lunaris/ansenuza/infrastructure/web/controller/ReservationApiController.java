package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.application.usecase.PersistPaymentReceiptUseCase;
import java.util.Map;
import java.security.Principal;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ReservationApiController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReceiptStoragePort receiptStoragePort;
    private final PersistPaymentReceiptUseCase persistPaymentReceiptUseCase;

    @PostMapping(value = {"/api/reservations", "/api/public/reservations"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestPart("reservation") CreateReservationRequest request,
            @RequestPart(value = "paymentReceipt", required = false) MultipartFile receipt) {
        String receiptUrl = receipt != null && !receipt.isEmpty()
                ? receiptStoragePort.uploadFile(receipt) : null;
        Reservation reservation = createReservationUseCase.executePublic(
                request.withSource(ReservationSource.WEB), receiptUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateReservationResponse.from(reservation));
    }

    @PostMapping(value = "/api/v1/reservations/{reservationCode}/receipt",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadReceipt(
            @org.springframework.web.bind.annotation.PathVariable String reservationCode,
            @RequestPart("file") MultipartFile file,
            Principal principal) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "message", "El comprobante es obligatorio."));
        }
        String url = receiptStoragePort.uploadFile(file);
        persistPaymentReceiptUseCase.executeByReservationCodeOwnedBy(
                reservationCode, url, "PASSENGER_WEB", principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "reservationCode", reservationCode,
                "paymentReceiptUrl", url));
    }
}
