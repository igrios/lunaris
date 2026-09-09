package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:manifest-repository;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class ReservationManifestRepositoryTest {
    private static final LocalDate DATE = LocalDate.of(2026, 9, 10);
    private final ReservationRepository reservations;
    private final PassengerRepository passengers;

    @Autowired
    ReservationManifestRepositoryTest(ReservationRepository reservations, PassengerRepository passengers) {
        this.reservations = reservations;
        this.passengers = passengers;
    }

    @ParameterizedTest
    @CsvSource({
            "12:00,12:00",
            "12:00 PM,12:00",
            "12:00PM,12:00",
            "12:00 hs,12:00",
            "' 12 : 00 pM ', '12:00 PM'",
            "14:00,14:00 hs",
            "14:00PM,14:00",
            "03:00 AM,03:00",
            "03:00,03:00AM"
    })
    void comparesHoursWithoutSuffixesOrSpaces(String stored, String filter) {
        var matching = save("Córdoba", "Miramar", stored, null, false, null, DATE, "CONFIRMED", null);
        save("Córdoba", "Miramar", "17:30", null, false, null, DATE, "CONFIRMED", null);
        assertThat(reservations.findActiveManifest(DATE, filter, true))
                .extracting(Reservation::getId).containsExactly(matching.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "Córdoba Capital,Miramar,false,,",
            "Cordoba,Miramar,false,,",
            "Aeropuerto,Miramar,false,,",
            "Morteros,Córdoba,true,,",
            "Morteros,Marull,false,VUELTA,",
            "Morteros,Córdoba,false,,LEGACY-VUELTA"
    })
    void acceptsEachIndependentReturnIndicator(String origin, String destination,
            boolean roundTrip, String direction, String code) {
        var matching = save(origin, destination, "14:00", direction, roundTrip, code,
                DATE, "CONFIRMED", null);
        assertThat(reservations.findActiveManifest(DATE, "14:00", true))
                .extracting(Reservation::getId).containsExactly(matching.getId());
    }

    @Test
    void preservesDateStatusAndOutboundFilters() {
        var outbound = save("Morteros", "Córdoba", "14:00", "IDA", false, "TEST-IDA",
                DATE, "CONFIRMED", null);
        save("Córdoba", "Miramar", "14:00", "VUELTA", false, null,
                DATE.plusDays(1), "CONFIRMED", null);
        save("Córdoba", "Miramar", "14:00", "VUELTA", false, null,
                DATE, "CANCELLED", null);
        for (var state : new Reservation.TravelStatus[] { Reservation.TravelStatus.OPEN_RETURN,
                Reservation.TravelStatus.COMPLETED, Reservation.TravelStatus.REALIZED,
                Reservation.TravelStatus.CANCELED, Reservation.TravelStatus.NO_SHOW }) {
            save("Córdoba", "Miramar", "14:00", "VUELTA", false, null, DATE, "CONFIRMED", state);
        }
        assertThat(reservations.findActiveManifest(DATE, "14:00", true)).isEmpty();
        assertThat(reservations.findActiveManifest(DATE, "14:00", false))
                .extracting(Reservation::getId).containsExactly(outbound.getId());
    }

    @Test
    void retainsLegacyDefaultForMissingSchedule() {
        var matching = save("Morteros", "Córdoba", null, "IDA", false, null,
                DATE, "CONFIRMED", null);
        assertThat(reservations.findActiveManifest(DATE, "03:00 hs", false))
                .extracting(Reservation::getId).containsExactly(matching.getId());
    }

    @Test
    void returnCapacityIncludesLinkedUndatedReturnsWithoutCountingOutboundTwice() {
        save("Morteros", "Córdoba", "03:00 AM", "IDA", true, "OPEN-IDA",
                DATE, "CONFIRMED", Reservation.TravelStatus.REALIZED);
        var open = save("Córdoba", "Morteros", null, "VUELTA", true, "OPEN-VUELTA",
                null, "CONFIRMED", Reservation.TravelStatus.OPEN_RETURN);
        var booked = save("Córdoba", "Morteros", "14:00", "VUELTA", true, "BOOKED-VUELTA",
                DATE, "CONFIRMED", Reservation.TravelStatus.CONFIRMED);
        save("Morteros", "Córdoba", "03:00 AM", "IDA", true, "OLD-IDA",
                DATE.minusDays(1), "CONFIRMED", Reservation.TravelStatus.REALIZED);
        save("Córdoba", "Morteros", null, "VUELTA", true, "OLD-VUELTA",
                null, "CONFIRMED", Reservation.TravelStatus.OPEN_RETURN);
        save("Córdoba", "Morteros", "14:00", "VUELTA", false, "CANCELLED-VUELTA",
                DATE, "CANCELLED", Reservation.TravelStatus.CANCELED);
        assertThat(reservations.findReturnCapacityCandidates(DATE))
                .extracting(Reservation::getId).containsExactlyInAnyOrder(open.getId(), booked.getId());
    }

    private Reservation save(String origin, String destination, String schedule, String direction,
            boolean roundTrip, String code, LocalDate date, String status, Reservation.TravelStatus travelStatus) {
        var passenger = passengers.save(Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone(UUID.randomUUID().toString()).build());
        return reservations.saveAndFlush(Reservation.builder().passenger(passenger)
                .pickupLocality(origin).destination(destination).departureSchedule(schedule)
                .routeDirection(direction).roundTrip(roundTrip).reservationCode(code)
                .travelDate(date).status(status).paymentVerified(false).travelStatus(travelStatus).build());
    }
}
