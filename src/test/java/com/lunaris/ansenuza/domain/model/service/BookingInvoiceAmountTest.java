package com.lunaris.ansenuza.domain.model.service;

import static org.assertj.core.api.Assertions.*;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingInvoiceAmountTest {
    @Test void countsExplicitRepeatedGroupTotalOnce() {
        assertThat(BookingInvoiceAmount.total(List.of(leg("89000", true), leg("89000", true))))
                .isEqualByComparingTo("89000");
    }
    @Test void keepsCorrectLegAmountsAndExtras() {
        var outbound = leg("44500", false);
        outbound.setExtraAmount(new BigDecimal("1000"));
        assertThat(BookingInvoiceAmount.total(List.of(outbound, leg("44500", false))))
                .isEqualByComparingTo("90000");
    }
    @Test void rejectsInconsistentGroupTotals() {
        assertThatThrownBy(() -> BookingInvoiceAmount.total(List.of(leg("89000", true), leg("44500", false))))
                .isInstanceOf(DomainValidationException.class);
    }
    private Reservation leg(String amount, boolean groupTotal) {
        return Reservation.builder().amount(new BigDecimal(amount)).amountIsGroupTotal(groupTotal).build();
    }
}
