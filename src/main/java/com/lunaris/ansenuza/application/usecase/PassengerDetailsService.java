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
import java.util.Optional;
import com.lunaris.ansenuza.shared.PhoneUtils;

@Service
@RequiredArgsConstructor
public class PassengerDetailsService {
    private final PassengerRepository repository;

    @Transactional(readOnly = true)
    public Passenger findById(UUID id) {
        return repository.findById(id).orElseThrow(PassengerNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Optional<PassengerLookup> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new DomainValidationException("El teléfono es obligatorio.");
        }
        var passengerByPhone = repository.findFirstByPhone(phone.strip());
        if (passengerByPhone.isEmpty()) {
            try {
                String normalized = PhoneUtils.normalizeArgentinePhone(phone);
                if (!normalized.equals(phone.strip())) {
                    passengerByPhone = repository.findFirstByPhone(normalized);
                }
            } catch (DomainValidationException ignored) {
                // Los registros históricos pueden usar teléfonos sin formato internacional.
            }
        }
        return passengerByPhone.map(passenger -> new PassengerLookup(
                passenger.getId(), passenger.getFirstName(), passenger.getLastName(),
                passenger.getCuil(), passenger.getPhone(), passenger.getAddress(),
                passenger.getLocality(), passenger.getNotes()));
    }

    public record PassengerLookup(UUID id, String firstName, String lastName, String cuil,
            String phone, String address, String locality, String notes) {}

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
