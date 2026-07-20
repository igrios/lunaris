package com.lunaris.ansenuza.infrastructure.web.dto.agenda;

import java.util.List;
import java.util.UUID;

public record EnviarHojaRutaRequest(
        String phone,
        List<UUID> reservationIds
) {}
