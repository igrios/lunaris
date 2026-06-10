package com.lunaris.ansenuza.application.usecase;

import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.CreatePassengerRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePassengerUseCase {

    private final PassengerRepository repository;

    public Passenger execute(CreatePassengerRequest request) {

        Passenger passenger = Passenger.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .cuil(request.cuil())
                .phone(request.phone())
                .address(request.address())
                .locality(request.locality())
                .build();

        return repository.save(passenger);
    }
}