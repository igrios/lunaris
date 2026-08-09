package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news-banners")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NewsBannerApiController {

    private final NewsBannerService service;

    @GetMapping({"", "/active"})
    public List<NewsBannerResponse> findActive() {
        return service.findActive().stream().map(NewsBannerResponse::from).toList();
    }

    public record NewsBannerResponse(
            UUID id,
            String title,
            String description,
            String imageUrl,
            String eventType,
            boolean hasWaitingList) {

        private static NewsBannerResponse from(NewsBanner banner) {
            return new NewsBannerResponse(
                    banner.getId(), banner.getTitle(), banner.getDescription(),
                    banner.getImageUrl(),
                    banner.getEventType() == null || banner.getEventType().isBlank()
                            ? "GENERAL" : banner.getEventType(),
                    banner.isHasWaitingList());
        }
    }
}
