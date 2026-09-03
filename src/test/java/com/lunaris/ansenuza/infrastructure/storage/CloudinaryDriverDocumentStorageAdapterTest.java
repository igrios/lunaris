package com.lunaris.ansenuza.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;

class CloudinaryDriverDocumentStorageAdapterTest {

    @Test
    void uploadsDriverDocumentToPersistentCloudStorage() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/raw/upload/driver-applications/licencia"));
        CloudinaryDriverDocumentStorageAdapter adapter = new CloudinaryDriverDocumentStorageAdapter(
                cloudinary, emptyLocalProvider(), new MockEnvironment(), "demo", "key", "secret");

        String url = adapter.store("licencia", new MockMultipartFile(
                "file", "licencia.pdf", "application/pdf", new byte[] {1}));

        assertEquals("https://res.cloudinary.com/demo/raw/upload/driver-applications/licencia", url);
    }

    @Test
    void productionWithoutCloudCredentialsRejectsVolatileStorage() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        LocalDriverDocumentStorageAdapter localStorage = mock(LocalDriverDocumentStorageAdapter.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<LocalDriverDocumentStorageAdapter> local = mock(ObjectProvider.class);
        when(local.getIfAvailable()).thenReturn(localStorage);
        CloudinaryDriverDocumentStorageAdapter adapter = new CloudinaryDriverDocumentStorageAdapter(
                mock(Cloudinary.class), local, environment, "", "", "");

        assertThrows(DomainValidationException.class, () -> adapter.store("licencia",
                new MockMultipartFile("file", "licencia.pdf", "application/pdf", new byte[] {1})));
        verifyNoInteractions(localStorage);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<LocalDriverDocumentStorageAdapter> emptyLocalProvider() {
        ObjectProvider<LocalDriverDocumentStorageAdapter> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }
}
