package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lunaris.ansenuza.application.dto.*;
import com.lunaris.ansenuza.domain.exception.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.infrastructure.web.controller.ReservationPanelEditController;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.springframework.transaction.event.TransactionalEventListener;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:integral-update;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.show-sql=false"
})
@Import({UpdateReservationUseCase.class, ReservationUpdateCapacityService.class,
        ReservationUpdatePricingService.class, UpdateReservationUseCaseTest.Support.class})
class UpdateReservationUseCaseTest {
    @TestConfiguration
    static class Support {
        @Bean ObjectMapper objectMapper() { return new ObjectMapper().registerModule(new JavaTimeModule()); }
        @Bean Validator validator() { return Validation.buildDefaultValidatorFactory().getValidator(); }
        @Bean CommitObserver observer() { return new CommitObserver(); }
    }
    static class CommitObserver {
        final List<ManualReservationCreated> passengerNotifications = new ArrayList<>();
        @TransactionalEventListener
        public void receive(ManualReservationCreated event) { passengerNotifications.add(event); }
        final List<OperatorNotification> notifications = new ArrayList<>();
        @TransactionalEventListener
        public void receive(OperatorNotification event) { notifications.add(event); }
    }
    @Autowired UpdateReservationUseCase update;
    @Autowired ReservationRepository reservations;
    @Autowired PassengerRepository passengers;
    @Autowired ReservationEventRepository audit;
    @Autowired ReservationUpdateOperationRepository operations;
    @Autowired ReservationUpdateStateRepository states;
    @Autowired InvoiceRepository invoices;
    @Autowired LocalityRepository localities;
    @Autowired FareRepository fares;
    @Autowired EntityManager em;
    @Autowired CommitObserver observer;
    @MockitoBean CapacityLockRepository locks;
    @MockitoBean PricingAndScheduleService pricing;
    private final LocalDate date = com.lunaris.ansenuza.shared.ArgentinaTime.today().plusDays(10);

    @BeforeEach
    void setup() {
        operations.deleteAllInBatch();
        states.deleteAllInBatch();
        audit.deleteAllInBatch();
        invoices.deleteAllInBatch();
        reservations.deleteAllInBatch();
        passengers.deleteAllInBatch();
        em.clear();
        when(locks.findForUpdate(anyString())).thenAnswer(inv -> new CapacityLock(inv.getArgument(0)));
        when(pricing.calculateReservationAmount(anyString(), anyString(), anyBoolean(), anyInt()))
                .thenAnswer(inv -> new BigDecimal(inv.<Boolean>getArgument(2) ? "100" : "70")
                        .multiply(BigDecimal.valueOf(inv.<Integer>getArgument(3))));
        observer.notifications.clear();
        observer.passengerNotifications.clear();
    }

    @Test
    void addsReturnPreservesRootAndReplaysWithoutDuplicateOrAudit() {
        Reservation root = root(2, "140", true);
        int revision = update.getState(root.getId()).revision();
        var request = request(revision, change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "18:45", null), null);
        var result = update.execute(root.getId(), request);
        assertThat(result.revision()).isGreaterThan(revision);
        assertThat(result.legs()).hasSize(2).anyMatch(r -> root.getId().equals(r.id()) && root.getReservationCode().equals(r.reservationCode()));
        assertThat(result.total()).isEqualByComparingTo("200");
        assertThat(result.pendingAmount()).isEqualByComparingTo("60");
        assertThat(result.legs()).filteredOn(r -> !root.getId().equals(r.id())).singleElement().satisfies(r -> {
            assertThat(r.departureSchedule()).isEqualTo("18:45");
            assertThat(r.travelDate()).isEqualTo(date.plusDays(1));
        });
        long auditCount = audit.count();
        assertThat(update.execute(root.getId(), request)).isEqualTo(result);
        assertThat(audit.count()).isEqualTo(auditCount);
        assertThat(update.getState(root.getId()).revision()).isEqualTo(result.revision());
        assertThatThrownBy(() -> update.execute(root.getId(), new UpdateReservationRequest(result.revision(),
                request.operationId(), null, null))).isInstanceOf(ReservationUpdateConflictException.class);
    }

    @Test
    void scheduledFlexibleOpenTransitionsKeepEstimateSeparateAndAcceptReturnId() {
        Reservation root = root(1, "70", false);
        var created = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "14:00", null), null));
        UUID returnId = returnId(root, created);
        var flexible = update.execute(returnId, request(created.revision(),
                change(ReturnAction.UPDATE, returnId, ReturnMode.FLEXIBLE, date.plusDays(2), null, "Después del médico"), null));
        assertThat(flexible.legs()).filteredOn(r -> r.id().equals(returnId)).singleElement().satisfies(r -> {
            assertThat(r.travelDate()).isNull();
            assertThat(r.departureSchedule()).isNull();
            assertThat(r.returnDate()).isEqualTo(date.plusDays(2));
            assertThat(r.flexibleReturnTime()).isTrue();
            assertThat(r.travelStatus()).isEqualTo(Reservation.TravelStatus.OPEN_RETURN);
        });
        var open = update.execute(root.getId(), request(flexible.revision(),
                change(ReturnAction.UPDATE, returnId, ReturnMode.OPEN, null, null, null), null));
        assertThat(open.legs()).filteredOn(r -> r.id().equals(returnId)).singleElement().satisfies(r -> {
            assertThat(r.flexibleReturnTime()).isFalse();
            assertThat(r.returnDate()).isNull();
        });
    }

    @Test
    void keepIsANoOpAndDoesNotGenerateEventsOrChangeRevision() {
        Reservation root = root(1, "70", false);
        var original = update.getState(root.getId());
        var result = update.execute(root.getId(), request(original.revision(), null, null));
        assertThat(result.legs()).isEqualTo(original.legs());
        assertThat(result.revision()).isEqualTo(original.revision());
        assertThat(audit.count()).isZero();
        assertThat(observer.notifications).isEmpty();
    }

    @Test
    void removeCreditsOnceAndAllowsAddingAnotherReturnWithCanceledHistory() {
        Reservation root = root(1, "70", true);
        var created = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "14:00", null),
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("70"))));
        UUID returnId = returnId(root, created);
        var removal = request(created.revision(), change(ReturnAction.REMOVE, returnId, null, null, null, null),
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("40")));
        var removed = update.execute(root.getId(), removal);
        assertThat(removed.creditedAmount()).isEqualByComparingTo("30");
        assertThat(removed.legs()).filteredOn(r -> r.id().equals(returnId)).singleElement()
                .satisfies(r -> assertThat(r.active()).isFalse());
        assertThat(reservations.findById(root.getId()).orElseThrow().getTripType()).isEqualTo(TripType.ONE_WAY);
        update.execute(root.getId(), removal);
        assertThat(passengers.findById(root.getPassenger().getId()).orElseThrow().getCurrentBalance()).isEqualByComparingTo("30");
        var added = update.execute(root.getId(), request(removed.revision(),
                change(ReturnAction.ADD, null, ReturnMode.FLEXIBLE, null, null, "Por la tarde"), null));
        assertThat(added.legs()).hasSize(3).filteredOn(UpdateReservationResult.Leg::active).hasSize(2);
    }

    @Test
    void staleRevisionFromLegacyWriterIsRejected() {
        Reservation root = root(1, "70", false);
        int revision = update.getState(root.getId()).revision();
        root.setPickupAddress("Cambio concurrente");
        reservations.saveAndFlush(root);
        assertThatThrownBy(() -> update.execute(root.getId(), request(revision, null, null)))
                .isInstanceOf(ReservationUpdateConflictException.class);
    }

    @Test
    void priceIncreaseKeepsRecognizedPaymentAndLaterDecreaseCreditsOnlyTheExcess() {
        Reservation root = root(1, "70", true);
        var increase = update.execute(root.getId(), request(0, null,
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("100.01"))));
        assertThat(increase.pendingAmount()).isEqualByComparingTo("30.01");
        assertThat(increase.legs().getFirst().paymentVerified()).isFalse();
        assertThat(increase.legs().getFirst().recognizedPaidAmount()).isEqualByComparingTo("70");
        var decrease = update.execute(root.getId(), request(increase.revision(), null,
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("60"))));
        assertThat(decrease.creditedAmount()).isEqualByComparingTo("10");
        assertThat(decrease.pendingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void appliedBalanceAndExtrasAreNotDuplicatedAndCentavosArePreserved() {
        Reservation root = root(1, "50", true);
        root.setUsedBalance(new BigDecimal("20"));
        root.setExtraAmount(new BigDecimal("10"));
        reservations.saveAndFlush(root);
        var result = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "17:30", null),
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("101.01"))));
        assertThat(result.total()).isEqualByComparingTo("101.01");
        assertThat(result.invoiceTotal()).isEqualByComparingTo("81.01");
        assertThat(result.pendingAmount()).isEqualByComparingTo("21.01");
        assertThat(result.legs().stream().map(UpdateReservationResult.Leg::usedBalance).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("20");
    }

    @Test
    void unavailableReturnRollsBackGroupPriceOperationAndNotification() {
        Reservation root = root(1, "70", true);
        Reservation occupied = root(9, "900", false);
        occupied.setPickupLocality("Córdoba"); occupied.setDestination("Morteros");
        occupied.setTravelDate(date.plusDays(1)); occupied.setDepartureSchedule("14:00");
        reservations.saveAndFlush(occupied);
        Reservation occupied2 = root(9, "900", false);
        occupied2.setPickupLocality("Córdoba"); occupied2.setDestination("Morteros");
        occupied2.setTravelDate(date.plusDays(1)); occupied2.setDepartureSchedule("14:00");
        reservations.saveAndFlush(occupied2);
        Reservation occupied3 = root(1, "100", false);
        occupied3.setPickupLocality("Córdoba"); occupied3.setDestination("Morteros");
        occupied3.setTravelDate(date.plusDays(1)); occupied3.setDepartureSchedule("14:00");
        reservations.saveAndFlush(occupied3);
        TestTransaction.flagForCommit(); TestTransaction.end(); TestTransaction.start();
        assertThatThrownBy(() -> update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "14:00", null), null)))
                .isInstanceOf(SeatCapacityExceededException.class);
        TestTransaction.flagForRollback(); TestTransaction.end(); TestTransaction.start();
        assertThat(reservations.findUpdateGroupIdsForUpdate(root.getBookingGroupCode(), root.getId())).hasSize(1);
        assertThat(operations.count()).isZero();
        assertThat(observer.notifications).isEmpty();
        assertThat(reservations.findById(root.getId()).orElseThrow().getAmount()).isEqualByComparingTo("70");
    }

    @Test
    void rejectsInvalidModesUnknownTargetsAndProtectedGroups() {
        Reservation root = root(1, "70", false);
        assertThatThrownBy(() -> update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.FLEXIBLE, null, "17:30", "Tarde"), null)))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> update.execute(root.getId(), request(0,
                change(ReturnAction.UPDATE, UUID.randomUUID(), ReturnMode.OPEN, null, null, null), null)))
                .isInstanceOf(DomainValidationException.class);
        root.setInvoiceIssued(true); reservations.saveAndFlush(root);
        int revision = update.getState(root.getId()).revision();
        assertThatThrownBy(() -> update.execute(root.getId(), request(revision, null,
                new PricingChangeRequest(PricingMode.RECALCULATE, null))))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void notificationIsDispatchedOnlyAfterCommit() {
        Reservation root = root(1, "70", false);
        update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.FLEXIBLE, date.plusDays(1), null, null), null));
        assertThat(observer.notifications).isEmpty();
        TestTransaction.flagForCommit(); TestTransaction.end();
        assertThat(observer.notifications).hasSize(1);
    }

    @Test
    void splitReturnUpdatesOnlyTheSelectedBlockAndKeepsTheOther() {
        Reservation root = root(2, "140", false);
        var created = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.OPEN, null, null, null), null));
        UUID firstId = returnId(root, created);
        Reservation first = reservations.findById(firstId).orElseThrow();
        first.setPassengerCount(1);
        first.setAmount(new BigDecimal("50"));
        Reservation second = first.toBuilder().id(null).createdAt(null).updatedAt(null)
                .reservationCode("SPLIT-" + UUID.randomUUID().toString().substring(0, 8)).build();
        second = reservations.saveAndFlush(second);
        UUID secondId = second.getId();
        int revision = update.getState(root.getId()).revision();
        var result = update.execute(root.getId(), request(revision,
                change(ReturnAction.UPDATE, firstId, ReturnMode.SCHEDULED, date.plusDays(1), "18:45", null), null));
        assertThat(result.legs()).hasSize(3).filteredOn(r -> r.id().equals(secondId)).singleElement()
                .satisfies(r -> assertThat(r.travelStatus()).isEqualTo(Reservation.TravelStatus.OPEN_RETURN));
        assertThat(result.legs()).filteredOn(r -> r.id().equals(firstId)).singleElement()
                .satisfies(r -> assertThat(r.travelDate()).isEqualTo(date.plusDays(1)));
    }

    @Test
    void cancellationAfterPriceIncreaseRefundsThePaymentAlreadyRecognized() {
        Reservation root = root(1, "70", true);
        update.execute(root.getId(), request(0, null,
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("100"))));
        var cancellation = new ReservationService(reservations, audit, passengers,
                mock(OnboardPassengerUseCase.class), locks, pricing);
        assertThat(cancellation.cancelReservation(root.getId(), "ADMIN_PANEL").creditedAmount()).isEqualByComparingTo("70");
        assertThat(passengers.findById(root.getPassenger().getId()).orElseThrow().getCurrentBalance()).isEqualByComparingTo("70");
    }

    @Test
    void callerRollbackRestoresCreditPriceOperationAndSuppressesNotification() {
        Reservation root = root(1, "70", true);
        TestTransaction.flagForCommit(); TestTransaction.end(); TestTransaction.start();
        var result = update.execute(root.getId(), request(0, null,
                new PricingChangeRequest(PricingMode.AGREED_TOTAL, new BigDecimal("40"))));
        assertThat(result.creditedAmount()).isEqualByComparingTo("30");
        TestTransaction.flagForRollback(); TestTransaction.end(); TestTransaction.start();
        assertThat(reservations.findById(root.getId()).orElseThrow().getAmount()).isEqualByComparingTo("70");
        assertThat(passengers.findById(root.getPassenger().getId()).orElseThrow().getCurrentBalance()).isEqualByComparingTo("0");
        assertThat(operations.count()).isZero();
        assertThat(observer.notifications).isEmpty();
    }

    @Test
    void canceledHistoryDoesNotPreventPayingOrInvoicingTheActiveGroup() {
        Reservation root = root(1, "70", false);
        var added = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.SCHEDULED, date.plusDays(1), "14:00", null), null));
        UUID canceledId = returnId(root, added);
        var removed = update.execute(root.getId(), request(added.revision(),
                change(ReturnAction.REMOVE, canceledId, null, null, null, null), null));
        new MarkTripAsPaidService(reservations, audit).markTripAsPaid(root.getId(), "TEST");
        Invoice invoice = new InvoicePersistenceService(invoices, em, reservations).persistUploadedInvoice(
                new InvoicePersistenceService.InvoiceData(root.getId(), "F-TEST", "Ana Pérez", "20-00000000-0",
                        removed.invoiceTotal(), "https://example.test/invoice.pdf"));
        assertThat(invoice.getAmount()).isEqualByComparingTo("70");
        assertThat(reservations.findById(canceledId).orElseThrow().isInvoiceIssued()).isFalse();
        assertThat(reservations.findById(root.getId()).orElseThrow().isInvoiceIssued()).isTrue();
    }

    @Test
    void groupWithoutLegacyCodeRemainsReachableFromEitherLeg() {
        Reservation root = root(1, "70", false);
        root.setBookingGroupCode(null); root.setReservationCode(null);
        reservations.saveAndFlush(root);
        var created = update.execute(root.getId(), request(0,
                change(ReturnAction.ADD, null, ReturnMode.FLEXIBLE, date.plusDays(1), null, null), null));
        UUID returnId = returnId(root, created);
        assertThat(update.getState(returnId).legs()).hasSize(2);
        assertThat(update.getState(root.getId()).revision()).isEqualTo(created.revision());
    }

    @Test
    void panelCorrectsClosedInvoicedReservationAndKeepsDocumentsAndLifecycle() throws Exception {
        Reservation root = root(2, "140", true);
        root.setStatus("COMPLETED");
        root.setTravelStatus(Reservation.TravelStatus.COMPLETED);
        root.setInvoiceIssued(true);
        root.setInvoiceUrl("https://example.test/factura.pdf");
        root.setPaymentReceiptUrl("https://example.test/comprobante.pdf");
        root.setNotes("Original");
        reservations.saveAndFlush(root);
        var invoice = invoices.saveAndFlush(Invoice.builder().reservationId(root.getId())
                .invoiceNumber("0001-00000123").amount(new BigDecimal("140"))
                .pdfUrl(root.getInvoiceUrl()).sentViaWhatsapp(true).build());
        String path = "/reservas-panel/" + root.getId() + "/editar";
        var mvc = panelMvc();
        var form = mvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(form).contains("action=\"" + path + "\"", "name=\"travelDate\"", "name=\"departureSchedule\"")
                .doesNotContain("name=\"agreedAmount\"", "/admin/chat");

        mvc.perform(post(path)
                        .principal(() -> "operador.test").param("expectedRevision", "0").param("operationId", "panel-closed")
                        .param("travelDate", date.minusDays(1).toString()).param("departureSchedule", "10:45")
                        .param("notes", "Corrección de agenda"))
                .andExpect(redirectedUrl("/reservas-panel"));

        em.flush();
        em.clear();
        var saved = reservations.findById(root.getId()).orElseThrow();
        assertThat(saved.getTravelDate()).isEqualTo(date.minusDays(1));
        assertThat(saved.getDepartureSchedule()).isEqualTo("10:45");
        assertThat(saved.getNotes()).isEqualTo("Corrección de agenda");
        assertThat(saved.getStatus()).isEqualTo("COMPLETED");
        assertThat(saved.getTravelStatus()).isEqualTo(Reservation.TravelStatus.COMPLETED);
        assertThat(saved.getPaymentReceiptUrl()).isEqualTo(root.getPaymentReceiptUrl());
        assertThat(saved.isInvoiceIssued()).isTrue();
        assertThat(invoices.findById(invoice.getId())).hasValueSatisfying(current -> {
            assertThat(current.getInvoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
            assertThat(current.getAmount()).isEqualByComparingTo("140");
            assertThat(current.getPdfUrl()).isEqualTo(saved.getInvoiceUrl());
            assertThat(current.getSentViaWhatsapp()).isTrue();
        });
        assertThat(operations.findAll()).singleElement().satisfies(operation -> {
            assertThat(operation.getTriggeredBy()).isEqualTo("operador.test");
            assertThat(operation.getBeforeJson()).contains("Original");
        });
        assertThat(saved.isManualNotificationPending()).isFalse();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertThat(observer.passengerNotifications).isEmpty();
        assertThat(observer.notifications).isEmpty();
    }

    @Test
    void panelEditsActiveTripAndRecognizesPartialPaymentWithoutNotification() throws Exception {
        var root = root(1, "70", false);
        String path = "/reservas-panel/" + root.getId() + "/editar";
        panelMvc().perform(post(path)
                        .principal(() -> "operador.test").param("expectedRevision", "0").param("operationId", "panel-active")
                        .param("travelDate", date.plusDays(2).toString()).param("departureSchedule", "13:30")
                        .param("seats", "2").param("companionNames", "Juan").param("notes", "Retiro acordado")
                        .param("pricingMode", "AGREED_TOTAL").param("agreedAmount", "120").param("paidAmount", "45"))
                .andExpect(redirectedUrl("/reservas-panel"));
        em.flush();
        em.clear();
        assertThat(reservations.findById(root.getId())).hasValueSatisfying(saved -> {
            assertThat(saved.getTravelDate()).isEqualTo(date.plusDays(2));
            assertThat(saved.getDepartureSchedule()).isEqualTo("13:30");
            assertThat(saved.getTotalSeats()).isEqualTo(2);
            assertThat(saved.getCompanionNames()).isEqualTo("Juan");
            assertThat(saved.getAmount()).isEqualByComparingTo("120");
            assertThat(saved.recognizedPayment()).isEqualByComparingTo("45");
            assertThat(saved.getPaymentVerified()).isFalse();
            assertThat(saved.isManualNotificationPending()).isFalse();
        });
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertThat(observer.passengerNotifications).isEmpty();
        assertThat(observer.notifications).isEmpty();
    }

    @Test
    void panelProtectsCommercialFieldsWhenInvoiceExistsOnlyInLedger() throws Exception {
        var root = root(1, "70", true);
        invoices.saveAndFlush(Invoice.builder().reservationId(root.getId())
                .invoiceNumber("F-1").amount(new BigDecimal("70")).sentViaWhatsapp(false).build());
        assertThatThrownBy(() -> update.editFromPanel(root.getId(), new PanelReservationEdit(
                0, "panel-locked", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, PricingMode.AGREED_TOTAL, new BigDecimal("99"), null),
                "operador.test")).isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("únicamente correcciones");
        assertThat(reservations.findById(root.getId()).orElseThrow().getAmount()).isEqualByComparingTo("70");
    }

    @Test
    void panelChangesRouteAndAddsScheduledReturnInTheSameOperation() throws Exception {
        var root = root(1, "70", false);
        localities.save(Locality.builder().name("Suardi").build());
        fares.save(Fare.builder().localityName("Suardi").amount(new BigDecimal("70")).build());
        String path = "/reservas-panel/" + root.getId() + "/editar";
        panelMvc().perform(post(path).principal(() -> "operador.test")
                        .param("expectedRevision", "0").param("operationId", "panel-route")
                        .param("origin", "Suardi").param("destination", "Córdoba")
                        .param("returnAction", "ADD").param("returnMode", "SCHEDULED")
                        .param("returnDate", date.plusDays(1).toString()).param("returnTime", "14:00"))
                .andExpect(redirectedUrl("/reservas-panel"));
        em.flush();
        em.clear();
        var group = reservations.findReservationGroup(root.getBookingGroupCode());
        assertThat(group).hasSize(2).anySatisfy(leg -> {
            assertThat(leg.getId()).isEqualTo(root.getId());
            assertThat(leg.getPickupLocality()).isEqualTo("Suardi");
        }).anySatisfy(leg -> {
            assertThat(leg.getPickupLocality()).isEqualTo("Córdoba");
            assertThat(leg.getDestination()).isEqualTo("Suardi");
            assertThat(leg.getDepartureSchedule()).isEqualTo("14:00");
            assertThat(leg.isManualNotificationPending()).isFalse();
        });
    }

    private MockMvc panelMvc() {
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(engine);
        return MockMvcBuilders.standaloneSetup(new ReservationPanelEditController(update))
                .setViewResolvers(resolver).build();
    }

    private Reservation root(int seats, String amount, boolean paid) {
        Passenger passenger = passengers.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez")
                .phone(UUID.randomUUID().toString()).build());
        String code = "T-" + UUID.randomUUID().toString().substring(0, 8);
        return reservations.saveAndFlush(Reservation.builder().passenger(passenger).passengerCount(seats)
                .reservationCode(code).bookingGroupCode(code).pickupLocality("Morteros").destination("Córdoba")
                .travelDate(date).departureSchedule("08:00 AM").routeDirection("IDA")
                .roundTrip(false).tripType(TripType.ONE_WAY).status("CONFIRMED")
                .travelStatus(Reservation.TravelStatus.SCHEDULED).amount(new BigDecimal(amount))
                .extraAmount(BigDecimal.ZERO).paymentVerified(paid).build());
    }

    private UpdateReservationRequest request(int revision, ReturnChangeRequest change, PricingChangeRequest pricing) {
        return new UpdateReservationRequest(revision, UUID.randomUUID().toString(), change, pricing);
    }

    private ReturnChangeRequest change(ReturnAction action, UUID target, ReturnMode mode, LocalDate date,
            String time, String preference) {
        return new ReturnChangeRequest(action, target == null ? null : target.toString(), mode, date, time, preference);
    }

    private UUID returnId(Reservation root, UpdateReservationResult result) {
        return result.legs().stream().filter(r -> !root.getId().equals(r.id()) && r.active()).findFirst().orElseThrow().id();
    }
}
