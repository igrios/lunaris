package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    @Cacheable(value = "passengersByPhone", key = "#phoneNumber")
    Optional<Passenger> findByPhone(String phoneNumber);

    @Override
    @CacheEvict(value = "passengersByPhone", key = "#passenger.phone")
    <S extends Passenger> S save(S passenger);

    @Override
    @CacheEvict(value = "passengersByPhone", key = "#passenger.phone")
    <S extends Passenger> S saveAndFlush(S passenger);

}
