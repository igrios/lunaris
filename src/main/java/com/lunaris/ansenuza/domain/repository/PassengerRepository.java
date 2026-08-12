package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import jakarta.persistence.LockModeType;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    Optional<Passenger> findByPhone(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.id = :id")
    Optional<Passenger> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.phone = :phone")
    Optional<Passenger> findByPhoneForUpdate(@Param("phone") String phone);

}
