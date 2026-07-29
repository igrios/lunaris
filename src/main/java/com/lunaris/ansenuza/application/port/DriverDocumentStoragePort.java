package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface DriverDocumentStoragePort {

    String store(String documentType, MultipartFile file);
}
