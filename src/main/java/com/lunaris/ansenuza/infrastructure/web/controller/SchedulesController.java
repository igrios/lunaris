package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.infrastructure.web.dto.ScheduleDto;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SchedulesController {

    private final PricingAndScheduleService scheduleService;

    @GetMapping({"/schedules", "/v1/schedules"})
    public List<ScheduleDto> schedules(
            @RequestParam(required = false) String pickupLocality,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = "dd/MM/yyyy")
            LocalDate travelDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = "dd/MM/yyyy")
            LocalDate date,
            @RequestParam(defaultValue = "false") boolean roundTrip,
            @RequestParam(defaultValue = "false") boolean openReturn) {
        LocalDate requestedDate = travelDate != null ? travelDate : date;
        LocalDate effectiveDate = requestedDate != null ? requestedDate : ArgentinaTime.today();

        return scheduleService.departureSchedules().stream()
                .map(schedule -> {
                    int availableSeats = scheduleService.availableSeats(effectiveDate, schedule);
                    String departureTime = schedule.substring(0, 5);
                    return new ScheduleDto(
                            departureTime, departureTime, availableSeats, availableSeats > 0);
                })
                .toList();
    }
}
