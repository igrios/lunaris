package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FleetCapacityService {

    public static final int OWN_VEHICLES = 2;
    public static final int SEATS_PER_VEHICLE = 4;
    public static final int OWN_FLEET_CAPACITY = OWN_VEHICLES * SEATS_PER_VEHICLE;

    private final BigDecimal externalDriverCost;

    public FleetCapacityService(
            @Value("${lunaris.fleet.external-driver-cost:0}")
            BigDecimal externalDriverCost) {
        this.externalDriverCost = externalDriverCost == null
                ? BigDecimal.ZERO
                : externalDriverCost.max(BigDecimal.ZERO);
    }

    public FleetSummary calculate(int totalPassengers) {
        int passengers = Math.max(totalPassengers, 0);
        int internalPassengers = Math.min(passengers, OWN_FLEET_CAPACITY);
        int externalPassengers = Math.max(passengers - OWN_FLEET_CAPACITY, 0);
        int externalVehicles = externalPassengers == 0
                ? 0
                : (int) Math.ceil((double) externalPassengers / SEATS_PER_VEHICLE);
        return new FleetSummary(
                passengers,
                internalPassengers,
                externalPassengers,
                externalVehicles,
                externalDriverCost.multiply(BigDecimal.valueOf(externalVehicles)));
    }

    public record FleetSummary(
            int totalPassengers,
            int internalPassengers,
            int externalPassengers,
            int externalVehicles,
            BigDecimal externalDriverExpense) {

        public boolean requiresExternalReinforcement() {
            return externalPassengers > 0;
        }
    }
}
