package com.lunaris.ansenuza.application.dto;

public record ScheduleDto(
        String id,
        String departureTime,
        String label,
        int availableSeats,
        boolean available) {
}
