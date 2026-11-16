package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;

/** Hora manual en formato HH:mm; la descripción es información adicional del regreso. */
public record ScheduleOpenReturnRequest(LocalDate travelDate, String departureSchedule,
        String returnCustomTime, String returnScheduleDescription, int cantidadVuelven) {}
