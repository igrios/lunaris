package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.PassengerNotFoundException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.UpdatePassengerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerDetailsService {
    private final PassengerRepository repository;

    @Transactional(readOnly = true)
    public Passenger findById(UUID id) {
        return repository.findById(id).orElseThrow(PassengerNotFoundException::new);
    }

    @Transactional
    public Passenger update(UUID id, UpdatePassengerRequest request) {
        if (request.firstName() == null || request.firstName().isBlank()
                || request.lastName() == null || request.lastName().isBlank()) {
            throw new DomainValidationException("El nombre y el apellido son obligatorios.");
        }
        Passenger passenger = findById(id);
        passenger.setFirstName(request.firstName().trim());
        passenger.setLastName(request.lastName().trim());
        passenger.setCuil(request.cuil());
        passenger.setPhone(request.phone());
        passenger.setAddress(request.address());
        passenger.setLocality(request.locality());
        passenger.setNotes(request.notes());
        return repository.save(passenger);
    }
}
