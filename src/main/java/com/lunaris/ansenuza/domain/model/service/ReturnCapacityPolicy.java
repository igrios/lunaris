package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

/** Shared return inventory. Undated returns conservatively retain seats in each block. */
public final class ReturnCapacityPolicy {
    public static final int CAPACITY = 8;
    private ReturnCapacityPolicy() {}

    public static String normalizeSchedule(String schedule) {
        if (schedule == null || schedule.isBlank()) return "03:00";
        String value = schedule.toUpperCase(Locale.ROOT).replaceAll("\\s+", "").replace("HS", "");
        boolean pm = value.endsWith("PM");
        boolean am = value.endsWith("AM");
        value = value.replace("AM", "").replace("PM", "");
        LocalTime time = LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("H:mm"));
        if (pm && time.getHour() < 12) time = time.plusHours(12);
        if (am && time.getHour() == 12) time = time.minusHours(12);
        return time.toString();
    }

    public static int availableSeats(ReservationRepository repository, LocalDate date, String schedule) {
        return availableSeats(repository, date, schedule, ArgentinaTime.now());
    }

    public static int availableSeats(ReservationRepository repository, LocalDate date,
            String schedule, LocalDateTime now) {
        boolean retain = now.isBefore(date.atTime(11, 0));
        long occupied = repository.findReturnCapacityCandidates(date).stream()
                .filter(r -> r.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                        ? retain : r.getDepartureSchedule() == null || r.getDepartureSchedule().isBlank()
                                || normalizeSchedule(r.getDepartureSchedule()).equals(normalizeSchedule(schedule)))
                .mapToLong(r -> r.getPassengerCount() == null ? 1 : Math.max(1, r.getPassengerCount()))
                .sum();
        return (int) Math.max(0, CAPACITY - occupied);
    }
}
