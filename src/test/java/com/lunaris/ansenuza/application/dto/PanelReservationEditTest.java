package com.lunaris.ansenuza.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PanelReservationEditTest {
    @Test
    void operationalCorrectionDoesNotRequestPricingOrReturnChanges() {
        var edit = edit(ReturnAction.KEEP, null, "");
        assertThat(edit.departureSchedule()).isNull();
        assertThat(edit.request().pricing()).isNull();
        assertThat(edit.request().returnChange()).isNull();
        assertThat(edit.changesCommercialFields()).isFalse();
    }

    @Test
    void originChangeIsAnOperationalCorrection() {
        var edit = new PanelReservationEdit(0, "panel-origin", null, null, null, null,
                "Brinkmann", null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(edit.origin()).isEqualTo("Brinkmann");
        assertThat(edit.changesCommercialFields()).isFalse();
    }

    @Test
    void flexibleReturnDoesNotForwardHiddenFixedTime() {
        var edit = edit(ReturnAction.UPDATE, ReturnMode.FLEXIBLE, "10:40");
        var change = edit.request().returnChange();
        assertThat(change.mode()).isEqualTo(ReturnMode.FLEXIBLE);
        assertThat(change.customTime()).isNull();
        assertThat(change.timePreference()).isEqualTo("Por la tarde");
        assertThat(edit.changesCommercialFields()).isTrue();
    }

    private PanelReservationEdit edit(ReturnAction action, ReturnMode mode, String time) {
        return new PanelReservationEdit(0, "panel", null, time, null, "Corrección",
                null, null, null, null, action, null, mode, LocalDate.of(2030, 1, 1),
                "14:00", "Por la tarde", null, null, null);
    }
}
