package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationViewController {

        private final PassengerRepository passengerRepository;
        // private final CreateReservationUseCase createReservationUseCase;
        private final ReservationRepository reservationRepository;
        private final LocalityRepository localityRepository;

        @GetMapping("/new")
        public String newReservation(Model model) {

                model.addAttribute("reservation", new CreateReservationForm());

                model.addAttribute("localities", localityRepository.findAll());

                return "reservation-form";
        }

        @PostMapping("/new")
        public String createReservation(@ModelAttribute CreateReservationForm form) {

                Passenger passenger = Passenger.builder().firstName(form.getFirstName())
                                .lastName(form.getLastName()).phone(form.getPhone())
                                .cuil(form.getCuil()).build();

                passenger = passengerRepository.save(passenger);

                Reservation reservation = Reservation.builder().passenger(passenger)
                                .travelDate(form.getTravelDate())
                                .pickupLocality(form.getPickupLocality())
                                .pickupAddress(form.getPickupAddress())
                                .destination(form.getDestination())
                                .paymentVerified(Boolean.TRUE.equals(form.getPaymentVerified()))
                                .notes(form.getNotes()).build();

                reservationRepository.save(reservation);

                return "redirect:/dashboard";
        }

}
