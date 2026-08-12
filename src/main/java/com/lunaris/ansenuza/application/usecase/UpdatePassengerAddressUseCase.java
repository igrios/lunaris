package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePassengerAddressUseCase {

    private final PassengerRepository passengerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(String phone, String address, String locality) {
        passengerRepository.findByPhoneForUpdate(phone).ifPresent(passenger -> {
            passenger.setAddress(address);
            passenger.setLocality(locality);
        });
    }
}
