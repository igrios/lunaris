package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmitDriverApplicationUseCase {

    private final DriverApplicationRepository repository;

    @Transactional
    public DriverApplication execute(DriverApplicationRequest request) {
        DriverApplication application = DriverApplication.builder()
                .fullName(request.fullName().trim())
                .phone(request.phone().trim())
                .vehicleModel(request.vehicleModel().trim())
                .licensePlate(request.licensePlate().trim().toUpperCase())
                .status(DriverApplication.Status.PENDING)
                .build();
        return repository.save(application);
    }
}
