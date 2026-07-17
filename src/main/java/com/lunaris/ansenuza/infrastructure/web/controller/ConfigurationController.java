package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final SystemConfigurationService configurationService;

    @GetMapping
    public List<SystemConfiguration> findAll() {
        return configurationService.findAll();
    }

    @GetMapping("/{key}")
    public SystemConfiguration findByKey(@PathVariable String key) {
        return configurationService.findByKey(key);
    }

    @PostMapping
    public ResponseEntity<SystemConfiguration> save(@RequestBody ConfigurationRequest request) {
        SystemConfiguration saved = configurationService.save(request.key(), request.value());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{key}")
    public ResponseEntity<SystemConfiguration> saveByKey(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        SystemConfiguration saved = configurationService.save(key, request.get("value"));
        return ResponseEntity.ok(saved);
    }

    public record ConfigurationRequest(String key, String value) {
    }
}
