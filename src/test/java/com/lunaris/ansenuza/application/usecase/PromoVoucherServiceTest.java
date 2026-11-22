package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.PromoVoucherService.Registration;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.PromoVoucher;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PromoVoucherRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PromoVoucherServiceTest {
    private final PromoVoucherRepository vouchers = mock(PromoVoucherRepository.class);
    private final CreateManualReservationUseCase manual = mock(CreateManualReservationUseCase.class);
    private final PromoVoucherService service = new PromoVoucherService(vouchers, manual,
            mock(LocalityRepository.class), mock(ScheduleService.class), mock(ReservationRepository.class));

    @Test
    void rejectsInvalidAmountsAndReceiptUrlsBeforeAllocatingNumber() {
        assertThatThrownBy(() -> service.register(input("100.001", null)))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("decimales");
        assertThatThrownBy(() -> service.register(input("100", "javascript:alert(1)")))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("URL");
        verifyNoInteractions(vouchers, manual);
    }

    @Test
    void cancelledVoucherCannotCreateATrip() {
        var promo = PromoVoucher.builder().number("LUN-PROMO-000001").build();
        promo.cancel();
        when(vouchers.findByNumberForUpdate(promo.getNumber())).thenReturn(Optional.of(promo));

        assertThatThrownBy(() -> service.redeem(promo.getNumber(), new Reservation(), null))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("ya no está disponible");
        verifyNoInteractions(manual);
    }

    private Registration input(String amount, String receipt) {
        return new Registration("Ana", "Pérez", "3511234567", "12345678", new BigDecimal(amount), receipt, null);
    }
}
