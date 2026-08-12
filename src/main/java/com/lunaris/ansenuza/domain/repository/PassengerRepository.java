package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import jakarta.persistence.LockModeType;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    @Cacheable(value = "passengersByPhone", key = "#phoneNumber")
    Optional<Passenger> findByPhone(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.id = :id")
    Optional<Passenger> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.phone = :phone")
    Optional<Passenger> findByPhoneForUpdate(@Param("phone") String phone);

    @Override
    @CacheEvict(value = "passengersByPhone", key = "#passenger.phone")
    <S extends Passenger> S save(S passenger);

    @Override
    @CacheEvict(value = "passengersByPhone", key = "#passenger.phone")
    <S extends Passenger> S saveAndFlush(S passenger);

}
