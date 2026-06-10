package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AgendaDayController {

    private final ReservationRepository reservationRepository;

    @GetMapping("/agenda/{date}")
    public String dayAgenda(
            @PathVariable LocalDate date,
            Model model) {

        List<Reservation> reservations =
                reservationRepository.findByTravelDate(date);

        model.addAttribute("date", date);
        model.addAttribute("reservations", reservations);

        return "agenda-day";
    }
}