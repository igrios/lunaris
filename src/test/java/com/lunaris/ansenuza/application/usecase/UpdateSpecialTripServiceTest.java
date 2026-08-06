package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UpdateSpecialTripServiceTest {
    @Test
    void reportsNotFoundInsteadOfCreatingOnUpdate() {
        SpecialTripRepositoryPort repository = mock(SpecialTripRepositoryPort.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        var service = new UpdateSpecialTripService(repository);
        var command = new SpecialTripCommand("Viaje", null, "A", "B", LocalDate.now(),
                LocalDate.now().plusDays(1), BigDecimal.TEN, 10, null, true);

        assertThatThrownBy(() -> service.update(99L, command))
                .isInstanceOf(SpecialTripNotFoundException.class);
    }
}
