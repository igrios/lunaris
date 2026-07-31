package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.shared.PhoneUtils;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
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
        DriverApplication application = DriverApplication.builder()
                .id(UUID.randomUUID())
                .fullName(request.fullName().trim())
                .phone(PhoneUtils.normalizeArgentinePhone(request.phone()))
                .locality("Sin especificar")
                .vehicleModel(request.vehicleModel().trim())
                .vehicleYear(request.vehicleYear())
                .licensePlate(request.licensePlate().trim().toUpperCase())
                .wantsDirectContact(false)
                .status(DriverApplication.Status.PENDING)
                .build();
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

        DriverApplication application = DriverApplication.builder()
                .id(UUID.randomUUID())
                .fullName(submission.fullName().trim())
                .phone(PhoneUtils.normalizeArgentinePhone(submission.phone()))
                .locality(submission.locality().trim())
                .vehicleModel(submission.vehicleModel().trim())
                .vehicleYear(submission.vehicleYear())
                .licensePlate(submission.plateNumber().trim().toUpperCase())
                .wantsDirectContact(submission.wantsDirectContact())
                .insuranceFileUrl(insuranceFileUrl)
                .greenCardFileUrl(greenCardFileUrl)
                .criminalRecordFileUrl(criminalRecordFileUrl)
                .status(DriverApplication.Status.PENDING)
                .build();
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
