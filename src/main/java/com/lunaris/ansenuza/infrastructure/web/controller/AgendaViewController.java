package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AgendaViewController {

    private final ReservationRepository reservationRepository;
@GetMapping("/agenda")
public String agenda(Model model) {

    LocalDate today = LocalDate.now();

    List<AgendaDayView> agenda =
            java.util.stream.IntStream.range(0, 5)
                    .mapToObj(today::plusDays)
                    .map(date -> {

                        List<Reservation> reservations =
                                reservationRepository.findByTravelDate(date);

                        int totalPassengers =
                                reservations.size();

                        int pendingPayments =
                                (int) reservations.stream()
                                        .filter(r ->
                                                !Boolean.TRUE.equals(
                                                        r.getPaymentVerified()))
                                        .count();

                        int estimatedVehicles =
                                totalPassengers == 0
                                        ? 0
                                        : (int) Math.ceil(
                                        totalPassengers / 4.0);

                        return new AgendaDayView(
                                date,
                                totalPassengers,
                                pendingPayments,
                                estimatedVehicles
                        );
                    })
                    .toList();

    model.addAttribute(
            "agenda",
            agenda
    );

    return "agenda";
}
}