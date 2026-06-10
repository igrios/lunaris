package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final GetDailyOperationSummaryUseCase useCase;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        DailyOperationSummaryResponse summary =
                useCase.execute(LocalDate.now());

        model.addAttribute("summary", summary);

        return "dashboard";
    }
}