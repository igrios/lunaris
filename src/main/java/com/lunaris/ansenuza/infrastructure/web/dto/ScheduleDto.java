package com.lunaris.ansenuza.infrastructure.web.dto;

public record ScheduleDto(
        String id,
        String departureTime,
        int availableSeats,
        boolean available) {
}
