package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.application.dto.*;
import com.lunaris.ansenuza.domain.exception.ReservationUpdateConflictException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Requiere una base PostgreSQL DESECHABLE: Hibernate crea y elimina las tablas del modelo. */
@EnabledIfEnvironmentVariable(named = "RESERVATION_UPDATE_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
        "spring.datasource.url=${RESERVATION_UPDATE_TEST_JDBC_URL}",
        "spring.datasource.username=${RESERVATION_UPDATE_TEST_DB_USER:ignacio}",
        "spring.datasource.password=${RESERVATION_UPDATE_TEST_DB_PASSWORD:}",
        "spring.datasource.hikari.maximum-pool-size=6"
})
@Import({UpdateReservationUseCase.class, ReservationUpdateCapacityService.class,
        ReservationUpdatePricingService.class, UpdateReservationUseCaseTest.Support.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UpdateReservationPostgresTest {
    @Autowired UpdateReservationUseCase update;
    @Autowired ReservationRepository reservations;
    @Autowired PassengerRepository passengers;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean PricingAndScheduleService pricing;
    private final LocalDate date = com.lunaris.ansenuza.shared.ArgentinaTime.today().plusDays(40);

    @BeforeEach
    void setup() {
        when(pricing.calculateReservationAmount(anyString(), anyString(), anyBoolean(), anyInt()))
                .thenAnswer(inv -> BigDecimal.valueOf(inv.<Boolean>getArgument(2) ? 100 : 70)
                        .multiply(BigDecimal.valueOf(inv.<Integer>getArgument(3))));
    }

    @Test
    void concurrentAddsToSameGroupAcceptOnlyOneRevision() throws Exception {
        UUID root = root(date, 1);
        var results = race(() -> attempt(root, add(date.plusDays(1), "14:00")),
                () -> attempt(root, add(date.plusDays(1), "14:00")));
        assertThat(results).containsExactlyInAnyOrder(true, false);
        assertThat(update.getState(root).legs()).hasSize(2);
    }

    @Test
    void duplicateConcurrentOperationReturnsSameResultWithoutCreatingAnotherLeg() throws Exception {
        UUID root = root(date.plusDays(3), 1);
        var request = add(date.plusDays(4), "17:30");
        var results = race(() -> update.execute(root, request), () -> update.execute(root, request));
        assertThat(results.get(0)).isEqualTo(results.get(1));
        assertThat(update.getState(root).legs()).hasSize(2);
    }

    @Test
    void twoGroupsCompetingForLastSeatNeverExceedCapacity() throws Exception {
        LocalDate travel = date.plusDays(6);
        // 18 butacas de regreso ya ocupadas, en dos reservas de nueve personas.
        for (int i = 0; i < 2; i++) {
            UUID occupied = root(travel, 9);
            new TransactionTemplate(transactions).executeWithoutResult(status -> {
                Reservation leg = reservations.findByIdForUpdate(occupied).orElseThrow();
                leg.setPickupLocality("Córdoba"); leg.setDestination("Morteros");
                leg.setRouteDirection("VUELTA"); leg.setDepartureSchedule("17:30");
            });
        }
        UUID first = root(travel.minusDays(1), 1);
        UUID second = root(travel.minusDays(1), 1);
        var results = race(() -> attempt(first, add(travel, "17:30")), () -> attempt(second, add(travel, "17:30")));
        assertThat(results).containsExactlyInAnyOrder(true, false);
        Integer available = new TransactionTemplate(transactions).execute(status ->
                ReturnCapacityPolicy.availableSeats(reservations, travel, "17:30"));
        assertThat(available).isZero();
        assertThat(update.getState(first).legs().size() + update.getState(second).legs().size()).isEqualTo(3);
    }

    @Test
    void crossDateMovesAcquireOldAndNewResourcesInSameOrder() throws Exception {
        UUID first = root(date.plusDays(8), 1);
        UUID second = root(date.plusDays(8), 1);
        var a = update.execute(first, add(date.plusDays(9), "14:00"));
        var b = update.execute(second, add(date.plusDays(10), "14:00"));
        UUID returnA = a.legs().stream().filter(l -> !l.id().equals(first)).findFirst().orElseThrow().id();
        UUID returnB = b.legs().stream().filter(l -> !l.id().equals(second)).findFirst().orElseThrow().id();
        var results = race(() -> update.execute(first, move(a.revision(), returnA, date.plusDays(10))),
                () -> update.execute(second, move(b.revision(), returnB, date.plusDays(9))));
        assertThat(results.get(0).legs()).filteredOn(l -> l.id().equals(returnA)).singleElement()
                .satisfies(l -> assertThat(l.travelDate()).isEqualTo(date.plusDays(10)));
        assertThat(results.get(1).legs()).filteredOn(l -> l.id().equals(returnB)).singleElement()
                .satisfies(l -> assertThat(l.travelDate()).isEqualTo(date.plusDays(9)));
    }

    @Test
    void concurrentCreditsForSamePassengerDoNotOverwriteCachedBalance() throws Exception {
        UUID first = root(date.plusDays(12), 1);
        UUID second = root(date.plusDays(12), 1);
        UUID passengerId = new TransactionTemplate(transactions).execute(status -> {
            Reservation a = reservations.findById(first).orElseThrow();
            Reservation b = reservations.findById(second).orElseThrow();
            b.setPassenger(a.getPassenger());
            a.setPaymentVerified(true); a.setPaymentStatus(PaymentStatus.PAID);
            b.setPaymentVerified(true); b.setPaymentStatus(PaymentStatus.PAID);
            return a.getPassenger().getId();
        });
        CountDownLatch loaded = new CountDownLatch(2);
        var results = race(() -> creditWithCachedPassenger(first, loaded), () -> creditWithCachedPassenger(second, loaded));
        assertThat(results).allSatisfy(result -> assertThat(result.creditedAmount()).isEqualByComparingTo("30"));
        assertThat(passengers.findById(passengerId).orElseThrow().getCurrentBalance()).isEqualByComparingTo("60");
    }

    private UpdateReservationResult creditWithCachedPassenger(UUID id, CountDownLatch loaded) {
        return new TransactionTemplate(transactions).execute(status -> {
            reservations.findById(id).orElseThrow().getPassenger().getCurrentBalance();
            loaded.countDown();
            try {
                if (!loaded.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("No se precargaron los pasajeros.");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt(); throw new IllegalStateException(exception);
            }
            return update.execute(id, new UpdateReservationRequest(0, UUID.randomUUID().toString(), null,
                    new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("40"))));
        });
    }

    private UUID root(LocalDate travelDate, int seats) {
        return new TransactionTemplate(transactions).execute(status -> {
            Passenger passenger = passengers.saveAndFlush(Passenger.builder().firstName("Prueba")
                    .lastName("Concurrente").phone(UUID.randomUUID().toString()).build());
            String code = "PG-" + UUID.randomUUID().toString().substring(0, 8);
            return reservations.saveAndFlush(Reservation.builder().passenger(passenger).passengerCount(seats)
                    .reservationCode(code).bookingGroupCode(code).pickupLocality("Morteros").destination("Córdoba")
                    .travelDate(travelDate).departureSchedule("08:00 AM").routeDirection("IDA")
                    .roundTrip(false).tripType(TripType.ONE_WAY).status("CONFIRMED")
                    .travelStatus(Reservation.TravelStatus.SCHEDULED).amount(BigDecimal.valueOf(70L * seats))
                    .extraAmount(BigDecimal.ZERO).paymentVerified(false).build()).getId();
        });
    }

    private UpdateReservationRequest add(LocalDate date, String time) {
        return new UpdateReservationRequest(0, UUID.randomUUID().toString(),
                new ReturnChangeRequest(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date, time, null), null);
    }

    private UpdateReservationRequest move(int revision, UUID target, LocalDate date) {
        return new UpdateReservationRequest(revision, UUID.randomUUID().toString(),
                new ReturnChangeRequest(ReturnAction.UPDATE, target.toString(), ReturnMode.SCHEDULED, date, "14:00", null), null);
    }

    private boolean attempt(UUID id, UpdateReservationRequest request) {
        try { update.execute(id, request); return true; }
        catch (ReservationUpdateConflictException | SeatCapacityExceededException expected) { return false; }
    }

    private <T> List<T> race(Supplier<T> first, Supplier<T> second) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks = List.of(first, second).stream().map(action -> executor.submit(() -> {
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("No comenzó la carrera.");
                return action.get();
            })).toList();
            start.countDown();
            return List.of(tasks.get(0).get(30, TimeUnit.SECONDS), tasks.get(1).get(30, TimeUnit.SECONDS));
        }
    }
}
