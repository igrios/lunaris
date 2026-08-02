package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface NewsBannerStoragePort {

    String upload(MultipartFile image);
}
