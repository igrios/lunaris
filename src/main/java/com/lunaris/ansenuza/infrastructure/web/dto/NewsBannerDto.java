package com.lunaris.ansenuza.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class NewsBannerDto {

    private UUID id;
    private String title;
    private String description;
    private String eventType;
    private Boolean hasWaitingList = false;
    private Boolean active = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    private String imageUrl;
    private MultipartFile image;
}
