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
                "Sin especificar",
                request.vehicleModel().trim(),
                request.vehicleYear(),
                request.licensePlate().trim().toUpperCase(),
                false);
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
                submission.locality().trim(),
                submission.vehicleModel().trim(),
                submission.vehicleYear(),
                submission.plateNumber().trim().toUpperCase(),
                submission.wantsDirectContact());
        application.updateDocuments(
                insuranceFileUrl, greenCardFileUrl, criminalRecordFileUrl);
        return repository.save(application);
    }

    private String storeIfPresent(String documentType, MultipartFile file) {
        return file == null || file.isEmpty() ? null : documentStorage.store(documentType, file);
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
