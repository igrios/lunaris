package com.lunaris.ansenuza.application.dto;

public record ScheduleDto(
        String id,
        String departureTime,
        int availableSeats,
        boolean available) {
}
