package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase.MultipartSubmission;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class DriverApplicationApiController {

    private final SubmitDriverApplicationUseCase submitDriverApplicationUseCase;

    @PostMapping(
            path = {"/api/drivers/apply", "/api/drivers/applications"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriverApplicationResponse> apply(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(value = "locality", required = false) String locality,
            @RequestParam(required = false) String vehicleModel,
            @RequestParam(required = false) Integer vehicleYear,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(defaultValue = "false") boolean wantsDirectContact,
            @RequestPart(value = "insuranceFile", required = false) MultipartFile insuranceFile,
            @RequestPart(value = "greenCardFile", required = false) MultipartFile greenCardFile,
            @RequestPart(value = "criminalRecordFile", required = false) MultipartFile criminalRecordFile) {
        String effectiveLicensePlate = licensePlate != null ? licensePlate : plateNumber;
        validateRequired(fullName, "fullName");
        validateRequired(phone, "phone");
        if (vehicleYear != null && vehicleYear <= 0) {
            throw new DomainValidationException("vehicleYear debe ser mayor a cero.");
        }

        DriverApplication application = submitDriverApplicationUseCase.execute(
                new MultipartSubmission(
                        fullName, phone,
                        locality,
                        vehicleModel, vehicleYear, effectiveLicensePlate,
                        wantsDirectContact),
                insuranceFile,
                greenCardFile,
                criminalRecordFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DriverApplicationResponse.from(application));
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " es obligatorio.");
        }
    }

    public record DriverApplicationResponse(
            UUID id,
            String status,
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String plateNumber,
            boolean wantsDirectContact) {

        static DriverApplicationResponse from(DriverApplication application) {
            return new DriverApplicationResponse(
                    application.getId(),
                    application.getStatus().name(),
                    application.getFullName(),
                    application.getPhone(),
                    application.getLocality(),
                    application.getVehicleModel(),
                    application.getVehicleYear(),
                    application.getLicensePlate(),
                    application.isWantsDirectContact());
        }
    }
}
