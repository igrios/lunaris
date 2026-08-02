package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.shared.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SubmitDriverApplicationUseCase {

    private static final String COMPANY_VEHICLE = "Unidad de Empresa";

    private final DriverApplicationRepository repository;
    private final DriverDocumentStoragePort documentStorage;

    @Transactional
    public DriverApplication execute(DriverApplicationRequest request) {
        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(request.phone());
        DriverApplication application = repository.findFirstByPhone(normalizedPhone)
                .orElseGet(DriverApplication::new);
        application.updateSubmission(
                request.fullName().trim(),
                normalizedPhone,
                normalizeVehicleModel(request.vehicleModel()),
                request.vehicleYear(),
                normalizeLicensePlate(request.licensePlate()),
                false);
        application.setLocality(normalizeLocality(request.locality()));
        application.updateDocuments(
                normalizeOptional(request.insuranceFileUrl()),
                normalizeOptional(request.greenCardFileUrl()),
                null);
        return repository.save(application);
    }

    @Transactional
    public DriverApplication execute(
            MultipartSubmission submission,
            MultipartFile insuranceFile,
            MultipartFile greenCardFile,
            MultipartFile criminalRecordFile) {
        String insuranceFileUrl = storeIfPresent("insurance", insuranceFile);
        String greenCardFileUrl = storeIfPresent("green-card", greenCardFile);
        String criminalRecordFileUrl = storeIfPresent("criminal-record", criminalRecordFile);

        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(submission.phone());
        DriverApplication application = repository.findFirstByPhone(normalizedPhone)
                .orElseGet(DriverApplication::new);
        application.updateSubmission(
                submission.fullName().trim(),
                normalizedPhone,
                normalizeVehicleModel(submission.vehicleModel()),
                submission.vehicleYear(),
                normalizeLicensePlate(submission.plateNumber()),
                submission.wantsDirectContact());
        application.setLocality(normalizeLocality(submission.locality()));
        application.updateDocuments(
                insuranceFileUrl, greenCardFileUrl, criminalRecordFileUrl);
        return repository.save(application);
    }

    private String storeIfPresent(String documentType, MultipartFile file) {
        return file == null || file.isEmpty() ? null : documentStorage.store(documentType, file);
    }

    private String normalizeLocality(String locality) {
        return locality == null || locality.isBlank() ? "Sin especificar" : locality.trim();
    }

    private String normalizeVehicleModel(String vehicleModel) {
        return vehicleModel == null || vehicleModel.isBlank()
                ? COMPANY_VEHICLE
                : vehicleModel.trim();
    }

    private String normalizeLicensePlate(String licensePlate) {
        return licensePlate == null || licensePlate.isBlank()
                ? null
                : licensePlate.trim().toUpperCase();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record MultipartSubmission(
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String plateNumber,
            boolean wantsDirectContact) {
    }
}
