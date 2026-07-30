package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DriverManagementServiceTest {

    @Test
    void createsDriverWithNormalizedFieldsAndSafeDefaults() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.saveAndFlush(any(Driver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DriverManagementService service = new DriverManagementService(repository);

        Driver saved = service.create("  Ana Pérez  ", "  543512345678  ", null, null);

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(repository).saveAndFlush(captor.capture());
        assertNotNull(saved.getId());
        assertEquals("Ana Pérez", saved.getFullName());
        assertEquals("543512345678", saved.getPhone());
        assertEquals(true, saved.isActive());
        assertEquals(null, saved.getRanking());
    }

    @Test
    void rejectsMissingRequiredFieldsBeforePersistence() {
        DriverManagementService service =
                new DriverManagementService(mock(DriverRepository.class));

        assertThrows(
                DomainValidationException.class,
                () -> service.create(" ", "543512345678", 3, true));
    }
}
