package com.lunaris.ansenuza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.PromoVoucherService.Registration;
import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.CapacityLock;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.PaymentStatus;
import com.lunaris.ansenuza.domain.model.PromoVoucher;
import com.lunaris.ansenuza.domain.model.PromoVoucher.Status;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.TripCategory;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.controller.PromoVoucherController;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:promo-vouchers;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
    "spring.datasource.username=sa", "spring.datasource.password="
})
@Import({PromoVoucherService.class, CreateManualReservationUseCase.class, ReservationService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PromoVoucherIntegrationTest {
    private static final String LIST = "/reservations/promos";
    private static final String RECEIPT = "https://lunaris.test/recibo.pdf";

    @Autowired PromoVoucherService promos;
    @Autowired ReservationRepository reservations;
    @Autowired LocalityRepository localities;
    @Autowired FareRepository fares;
    @Autowired DataSource dataSource;
    @MockitoBean ScheduleService schedules;
    @MockitoBean OnboardPassengerUseCase onboarding;
    @MockitoBean PricingAndScheduleService pricing;
    @MockitoBean CapacityLockRepository capacity;
    private MockMvc mvc;

    @BeforeAll
    void installPromoMigration() {
        new JdbcTemplate(dataSource).execute("DROP TABLE promo_vouchers");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V141__create_promo_vouchers.sql"))
                .execute(dataSource);
    }

    @BeforeEach
    void configureMvc() {
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        templates.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(engine);
        resolver.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new PromoVoucherController(promos)).setViewResolvers(resolver).build();
    }

    @Test
    void createsWithoutTripAndRendersSearchAndPassengerMessage() throws Exception {
        long tripCount = reservations.count();
        var result = mvc.perform(post(LIST).param("firstName", "Lucía").param("lastName", "Sosa")
                        .param("phone", "3562401111").param("cuil", "12345678")
                        .param("paidAmount", "15250.25").param("notes", "Seña en oficina").param("receiptUrl", RECEIPT))
                .andExpect(status().is3xxRedirection()).andReturn();
        var detail = result.getResponse().getRedirectedUrl();
        assertThat(detail).matches(LIST + "/LUN-PROMO-[0-9]{6,}");
        var number = detail.substring(detail.lastIndexOf('/') + 1);
        PromoVoucher first = promos.find(number);
        PromoVoucher other = register("Bruno", "3562402222");

        assertThat(other.getNumber()).isNotEqualTo(number);
        assertThat(first.getId()).isNotNull();
        assertThat(first.getStatus()).isEqualTo(Status.DISPONIBLE);
        assertThat(first.getReservationId()).isNull();
        assertThat(first.getRedeemedAt()).isNull();
        assertThat(reservations.count()).isEqualTo(tripCount);

        mvc.perform(get(detail)).andExpect(status().isOk())
                .andExpect(content().string(containsString(first.getPassengerMessage())))
                .andExpect(content().string(containsString("https://wa.me/543562401111?text=")));
        for (String query : List.of("lucía sosa", "+54 9 3562 401111", number.toLowerCase(java.util.Locale.ROOT))) {
            mvc.perform(get(LIST).param("q", query).param("status", "DISPONIBLE"))
                    .andExpect(status().isOk()).andExpect(content().string(containsString(number)))
                    .andExpect(content().string(not(containsString(other.getNumber()))));
        }
        mvc.perform(get(LIST).param("q", "Lucía").param("status", "CANJEADA"))
                .andExpect(content().string(containsString("No se encontraron promos.")));
    }

    @Test
    void webRedemptionCreatesAgendaTripAndPreservesDeposit() throws Exception {
        PromoVoucher promo = register("Emilia", "3562403333");
        String detail = LIST + "/" + promo.getNumber();
        var date = ArgentinaTime.today().plusDays(12);

        mvc.perform(get(detail + "/redeem")).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + detail + "/redeem\"")))
                .andExpect(content().string(containsString("Confirmar canje")));
        mvc.perform(post(detail + "/redeem").param("tripCategory", "SPECIAL")
                        .param("travelDate", date.toString()).param("departureSchedule", "09:30")
                        .param("originCustom", "Suardi").param("destinationCustom", "Alta Gracia")
                        .param("passengerCount", "1").param("customPrice", "25000.00")
                        .param("phone", "3511111111"))
                .andExpect(redirectedUrl(detail));

        PromoVoucher redeemed = promos.find(promo.getNumber());
        assertThat(redeemed.getStatus()).isEqualTo(Status.CANJEADA);
        assertThat(redeemed.getRedeemedAt()).isNotNull();
        assertThat(reservations.findSpecialAgendaTrips(date)).extracting(Reservation::getId)
                .contains(redeemed.getReservationId());
        assertThat(reservations.findById(redeemed.getReservationId())).hasValueSatisfying(trip -> {
            assertThat(trip.getPassenger().getPhone()).isEqualTo(promo.getPhone());
            assertThat(trip.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(trip.recognizedPayment()).isEqualByComparingTo(promo.getPaidAmount());
            assertThat(trip.getPaymentReceiptUrl()).isEqualTo(RECEIPT);
        });
        mvc.perform(get(detail)).andExpect(content().string(containsString("Ver en agenda")))
                .andExpect(content().string(not(containsString("Canjear promo"))));
    }

    @Test
    void competingRedemptionsCreateOnlyOneReservation() throws Exception {
        PromoVoucher promo = register("Paula", "3562404444");
        long tripCount = reservations.count();
        var barrier = new CyclicBarrier(2);
        Callable<Boolean> attempt = () -> {
            barrier.await(10, TimeUnit.SECONDS);
            try {
                promos.redeem(promo.getNumber(), specialTrip("15100.25"), null);
                return true;
            } catch (DomainValidationException exception) {
                assertThat(exception).hasMessageContaining("ya no está disponible");
                return false;
            }
        };
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(reservations.count()).isEqualTo(tripCount + 1);
        var redeemed = promos.find(promo.getNumber());
        assertThat(reservations.findById(redeemed.getReservationId())).hasValueSatisfying(trip -> {
            assertThat(trip.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(trip.recognizedPayment()).isEqualByComparingTo(promo.getPaidAmount());
        });
    }

    @Test
    void regularRedemptionKeepsVoucherAvailableOnFailureAndSplitsDepositAcrossLegs() {
        var promo = register("Marta", "3562405555");
        var date = ArgentinaTime.today().plusDays(19);
        var returnDate = date.plusDays(1);
        localities.save(Locality.builder().name("Morteros").build());
        fares.save(Fare.builder().localityName("Morteros").amount(BigDecimal.TEN).build());
        when(schedules.getSchedulesForWeb("Morteros", date))
                .thenReturn(List.of(new ScheduleDto("08:00", "08:00", "08:00", 19, true)));
        when(schedules.getReturnSchedulesForWeb(returnDate))
                .thenReturn(List.of(new ScheduleDto("14:00", "14:00", "14:00", 19, true)));
        var trip = Reservation.builder().tripCategory(TripCategory.REGULAR).tripType(TripType.ROUND_TRIP)
                .travelDate(date).returnDate(returnDate).roundTrip(true).departureSchedule("08:00")
                .pickupLocality("Morteros").destination("Córdoba").passengerCount(1)
                .amount(new BigDecimal("20000.01")).build();

        assertThatThrownBy(() -> promos.redeem(promo.getNumber(), trip, "14:00"))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("capacidad");
        assertThat(promos.find(promo.getNumber()).getStatus()).isEqualTo(Status.DISPONIBLE);
        assertThat(reservations.findDailyManifest(date)).isEmpty();

        when(capacity.findForUpdate(date + "|08:00|OUTBOUND")).thenReturn(new CapacityLock("outbound"));
        when(capacity.findForUpdate(returnDate + "|DAY|RETURN")).thenReturn(new CapacityLock("return"));
        var primary = promos.redeem(promo.getNumber(), trip, "14:00");
        var outbound = reservations.findDailyManifest(date).getFirst();
        var inbound = reservations.findDailyManifest(returnDate).getFirst();
        assertThat(outbound.getId()).isEqualTo(primary.getId());
        assertThat(outbound.getBookingGroupCode()).isEqualTo(inbound.getBookingGroupCode());
        assertThat(inbound.getDepartureSchedule()).isEqualTo("14:00");
        assertThat(outbound.recognizedPayment().add(inbound.recognizedPayment()))
                .isEqualByComparingTo(promo.getPaidAmount());
        assertThat(promos.find(promo.getNumber()).getReservationId()).isEqualTo(primary.getId());
    }

    private PromoVoucher register(String firstName, String phone) {
        return promos.register(new Registration(firstName, "Sosa", phone, "12345678",
                new BigDecimal("15100.25"), RECEIPT, null));
    }

    private Reservation specialTrip(String price) {
        return Reservation.builder().tripCategory(TripCategory.SPECIAL).tripType(TripType.ONE_WAY)
                .travelDate(ArgentinaTime.today().plusDays(15)).departureSchedule("10:20")
                .originCustom("Suardi").destinationCustom("Córdoba").passengerCount(1)
                .customPrice(new BigDecimal(price)).roundTrip(false).build();
    }
}
