package com.lunaris.ansenuza.infrastructure.storage;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!prod & !production")
public class LocalDriverDocumentStorageAdapter implements DriverDocumentStoragePort {

    private final Path storageDirectory;

    public LocalDriverDocumentStorageAdapter(
            @Value("${storage.driver-applications-dir:./data/driver-applications/}")
                    String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(String documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException(
                    "El archivo " + documentType + " es obligatorio.");
        }
        String extension = extension(file.getOriginalFilename());
        String fileName = documentType + "_" + UUID.randomUUID() + extension;
        Path destination = storageDirectory.resolve(fileName).normalize();
        if (!destination.startsWith(storageDirectory)) {
            throw new DomainValidationException("Nombre de archivo inválido.");
        }
        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(destination);
            return destination.toString();
        } catch (IOException exception) {
            throw new DomainValidationException(
                    "No se pudo almacenar el archivo " + documentType + ".");
        }
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        String name = Path.of(originalFilename).getFileName().toString();
        int separator = name.lastIndexOf('.');
        if (separator < 0 || separator == name.length() - 1) {
            return "";
        }
        String extension = name.substring(separator).toLowerCase();
        return extension.length() <= 10 ? extension : "";
    }
}
