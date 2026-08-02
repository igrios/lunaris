package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news-banners")
@RequiredArgsConstructor
public class NewsBannerApiController {

    private final NewsBannerService service;

    @GetMapping
    public List<NewsBannerResponse> findActive() {
        return service.findActive().stream().map(NewsBannerResponse::from).toList();
    }

    public record NewsBannerResponse(
            UUID id,
            String imageUrl,
            String title,
            boolean active,
            LocalDate validUntil,
            LocalDateTime createdAt) {

        private static NewsBannerResponse from(NewsBanner banner) {
            return new NewsBannerResponse(
                    banner.getId(), banner.getImageUrl(), banner.getTitle(), banner.isActive(),
                    banner.getValidUntil(), banner.getCreatedAt());
        }
    }
}
