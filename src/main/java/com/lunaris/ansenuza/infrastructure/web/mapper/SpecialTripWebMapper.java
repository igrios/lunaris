package com.lunaris.ansenuza.infrastructure.web.mapper;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import org.springframework.stereotype.Component;

@Component
public class SpecialTripWebMapper {
    public SpecialTripCommand toCommand(SpecialTripRequest request) {
        return new SpecialTripCommand(request.title(), request.description(), request.origin(),
                request.destination(), request.startDate(), request.endDate(), request.price(),
                request.maxPassengers(), request.imageUrl(), request.active());
    }

    public SpecialTripResponse toResponse(SpecialTrip trip) {
        return new SpecialTripResponse(trip.id(), trip.title(), trip.description(), trip.origin(),
                trip.destination(), trip.startDate(), trip.endDate(), trip.price(), trip.maxPassengers(),
                trip.imageUrl(), trip.active(), trip.createdAt());
    }
}
