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
            path = "/api/drivers/applications",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriverApplicationResponse> apply(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String locality,
            @RequestParam String vehicleModel,
            @RequestParam Integer vehicleYear,
            @RequestParam String plateNumber,
            @RequestParam boolean wantsDirectContact,
            @RequestPart("insuranceFile") MultipartFile insuranceFile,
            @RequestPart("greenCardFile") MultipartFile greenCardFile,
            @RequestPart("criminalRecordFile") MultipartFile criminalRecordFile) {
        validateRequired(fullName, "fullName");
        validateRequired(phone, "phone");
        validateRequired(locality, "locality");
        validateRequired(vehicleModel, "vehicleModel");
        validateRequired(plateNumber, "plateNumber");
        if (vehicleYear == null || vehicleYear <= 0) {
            throw new DomainValidationException("vehicleYear debe ser mayor a cero.");
        }

        DriverApplication application = submitDriverApplicationUseCase.execute(
                new MultipartSubmission(
                        fullName, phone, locality, vehicleModel, vehicleYear, plateNumber,
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
