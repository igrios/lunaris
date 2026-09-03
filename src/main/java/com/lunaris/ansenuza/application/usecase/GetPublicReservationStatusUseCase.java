package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPublicReservationStatusUseCase {

    private final ReservationRepository repository;

    @Transactional(readOnly = true)
    public PublicReservationStatus execute(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        return repository.findByReservationCode(code)
                .map(reservation -> new PublicReservationStatus(
                        reservation.getReservationCode(),
                        reservation.getStatus(),
                        reservation.getTravelStatus() == null
                                ? null : reservation.getTravelStatus().name(),
                        reservation.getTravelDate()))
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
    }

    public record PublicReservationStatus(
            String reservationCode, String status, String travelStatus, LocalDate travelDate) {
    }
}
