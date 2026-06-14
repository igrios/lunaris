package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

Optional<Passenger> findByPhone(String phone);

}