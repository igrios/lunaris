package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/driver-applications")
@RequiredArgsConstructor
public class DriverApplicationController {

    private final DriverApplicationManagementService managementService;

    @GetMapping
    public List<DriverApplication> findPending() {
        return managementService.findPending();
    }

    @PutMapping("/{id}/approve")
    public DriverApplication approve(@PathVariable UUID id) {
        return managementService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public DriverApplication reject(@PathVariable UUID id) {
        return managementService.reject(id);
    }
}
