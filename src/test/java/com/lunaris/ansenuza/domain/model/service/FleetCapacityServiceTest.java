package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FleetCapacityServiceTest {

    private final FleetCapacityService service =
            new FleetCapacityService(new BigDecimal("75000"));

    @Test
    void ownFleetCoversUpToEightPassengers() {
        var summary = service.calculate(8);

        assertEquals(8, summary.internalPassengers());
        assertEquals(0, summary.externalPassengers());
        assertEquals(0, summary.externalVehicles());
        assertEquals(BigDecimal.ZERO, summary.externalDriverExpense());
        assertFalse(summary.requiresExternalReinforcement());
    }

    @Test
    void ninthPassengerTriggersOneExternalDriver() {
        var summary = service.calculate(9);

        assertEquals(8, summary.internalPassengers());
        assertEquals(1, summary.externalPassengers());
        assertEquals(1, summary.externalVehicles());
        assertEquals(new BigDecimal("75000"), summary.externalDriverExpense());
        assertTrue(summary.requiresExternalReinforcement());
    }

    @Test
    void externalDriversAreGroupedInFourSeatUnits() {
        var summary = service.calculate(13);

        assertEquals(5, summary.externalPassengers());
        assertEquals(2, summary.externalVehicles());
        assertEquals(new BigDecimal("150000"), summary.externalDriverExpense());
    }
}
