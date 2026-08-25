package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.Reservation;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:reservation-promotion-repository;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class ReservationPromotionRepositoryTest {

    @Autowired private ReservationRepository reservations;
    @Autowired private PassengerRepository passengers;
    @Autowired private PromotionRepository promotions;

    @Test
    void detectsActiveUsageAndIgnoresCancelledReservationForSamePhoneAndPromotion() {
        Promotion promotion = new Promotion();
        promotion.setCode("1234");
        promotion.setDiscountPercentage(20);
        promotion.setMassive(true);
        promotion = promotions.saveAndFlush(promotion);
        Passenger passenger = passengers.saveAndFlush(Passenger.builder()
                .firstName("Ana").lastName("Pérez").phone("543511111111").build());

        Reservation cancelled = reservation(passenger, promotion, "CANCELLED");
        reservations.saveAndFlush(cancelled);
        assertThat(reservations.existsActivePromotionUsageByPhone(
                passenger.getPhone(), promotion.getId(), promotion.getCode())).isFalse();

        Reservation active = reservation(passenger, promotion, "PENDING_PAYMENT");
        reservations.saveAndFlush(active);
        assertThat(reservations.existsActivePromotionUsageByPhone(
                passenger.getPhone(), promotion.getId(), promotion.getCode())).isTrue();
    }

    private Reservation reservation(Passenger passenger, Promotion promotion, String status) {
        return Reservation.builder()
                .passenger(passenger)
                .travelDate(LocalDate.of(2026, 9, 15))
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .promotionId(promotion.getId())
                .promotionCode(promotion.getCode())
                .paymentVerified(false)
                .status(status)
                .build();
    }
}
