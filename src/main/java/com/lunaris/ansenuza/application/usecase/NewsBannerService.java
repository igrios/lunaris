package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import com.lunaris.ansenuza.domain.repository.NewsBannerRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NewsBannerService {

    private final NewsBannerRepository repository;
    private final NewsBannerStoragePort storage;

    @Transactional(readOnly = true)
    public List<NewsBanner> findActive() {
        List<NewsBanner> banners = repository.findActiveOn(ArgentinaTime.today());
        return banners == null ? List.of()
                : banners.stream().filter(java.util.Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public List<NewsBanner> findAll() {
        List<NewsBanner> banners = repository.findAllByOrderByCreatedAtDesc();
        return banners == null ? List.of()
                : banners.stream().filter(java.util.Objects::nonNull).toList();
    }

    @Transactional
    public NewsBanner create(
            String title, boolean active, LocalDate validUntil, MultipartFile image) {
        return create(title, null, null, false, active, validUntil, null, image);
    }

    @Transactional
    public NewsBanner create(String title, String description, String eventType,
            boolean hasWaitingList, boolean active, LocalDate validUntil,
            String externalImageUrl, MultipartFile image) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("El título es obligatorio.");
        }
        if (title.trim().length() > 150) {
            throw new DomainValidationException("El título no puede superar los 150 caracteres.");
        }
        NewsBanner banner = new NewsBanner();
        banner.setId(UUID.randomUUID());
        banner.setTitle(title.trim());
        banner.setDescription(normalizeOptional(description));
        banner.setEventType(normalizeEventType(eventType, title));
        banner.setHasWaitingList(hasWaitingList);
        banner.setImageUrl(resolveImageUrl(externalImageUrl, image));
        banner.setActive(active);
        banner.setValidUntil(validUntil);
        return repository.save(banner);
    }

    @Transactional(readOnly = true)
    public Map<String, String> findEventLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        findAll().stream()
                .filter(banner -> banner.getEventType() != null
                        && !banner.getEventType().isBlank())
                .forEach(banner -> labels.putIfAbsent(
                        banner.getEventType(), banner.getTitle()));
        return labels;
    }

    private String resolveImageUrl(String externalImageUrl, MultipartFile image) {
        if (externalImageUrl != null && !externalImageUrl.isBlank()) {
            String url = externalImageUrl.trim();
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                throw new DomainValidationException("La URL externa del flyer no es válida.");
            }
            return url;
        }
        if (image == null || image.isEmpty()) {
            throw new DomainValidationException("Debés subir un flyer o indicar una URL externa.");
        }
        return storage.upload(image);
    }

    private String normalizeEventType(String eventType, String title) {
        String source = eventType == null || eventType.isBlank() ? title : eventType;
        String normalized = Normalizer.normalize(source.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (normalized.isBlank()) {
            throw new DomainValidationException("No se pudo generar el código del evento.");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new DomainValidationException("La novedad indicada no existe.");
        }
        repository.deleteById(id);
    }
}
