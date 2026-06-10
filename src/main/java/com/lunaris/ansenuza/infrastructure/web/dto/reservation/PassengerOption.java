package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.util.UUID;

public record PassengerOption(

        UUID id,

        String fullName
) {
}