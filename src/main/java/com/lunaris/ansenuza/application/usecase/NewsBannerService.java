package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import com.lunaris.ansenuza.domain.repository.NewsBannerRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.List;
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
        return repository.findActiveOn(ArgentinaTime.today());
    }

    @Transactional(readOnly = true)
    public List<NewsBanner> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public NewsBanner create(
            String title, boolean active, LocalDate validUntil, MultipartFile image) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("El título es obligatorio.");
        }
        if (title.trim().length() > 150) {
            throw new DomainValidationException("El título no puede superar los 150 caracteres.");
        }
        NewsBanner banner = new NewsBanner();
        banner.setId(UUID.randomUUID());
        banner.setTitle(title.trim());
        banner.setImageUrl(storage.upload(image));
        banner.setActive(active);
        banner.setValidUntil(validUntil);
        return repository.save(banner);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new DomainValidationException("La novedad indicada no existe.");
        }
        repository.deleteById(id);
    }
}
