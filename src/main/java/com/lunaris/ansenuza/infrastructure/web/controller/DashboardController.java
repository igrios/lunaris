package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDailyOperationSummaryUseCase useCase;

    @GetMapping("/date/{travelDate}")
    public DailyOperationSummaryResponse getSummary(
            @PathVariable LocalDate travelDate) {

        return useCase.execute(travelDate);
    }
}