package com.lunaris.ansenuza.infrastructure.persistence.mapper;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.infrastructure.persistence.entity.SpecialTripEntity;
import org.springframework.stereotype.Component;

@Component
public class SpecialTripPersistenceMapper {
    public SpecialTripEntity toEntity(SpecialTrip domain) {
        SpecialTripEntity entity = new SpecialTripEntity();
        entity.setId(domain.id());
        entity.setTitle(domain.title());
        entity.setDescription(domain.description());
        entity.setOrigin(domain.origin());
        entity.setDestination(domain.destination());
        entity.setStartDate(domain.startDate());
        entity.setEndDate(domain.endDate());
        entity.setPrice(domain.price());
        entity.setMaxPassengers(domain.maxPassengers());
        entity.setImageUrl(domain.imageUrl());
        entity.setActive(domain.active());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }

    public SpecialTrip toDomain(SpecialTripEntity entity) {
        return new SpecialTrip(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getOrigin(),
                entity.getDestination(), entity.getStartDate(), entity.getEndDate(), entity.getPrice(),
                entity.getMaxPassengers(), entity.getImageUrl(), entity.isActive(), entity.getCreatedAt());
    }
}
