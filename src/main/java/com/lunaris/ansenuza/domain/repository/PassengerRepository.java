package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {


}