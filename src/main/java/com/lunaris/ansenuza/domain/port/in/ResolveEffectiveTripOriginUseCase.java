package com.lunaris.ansenuza.domain.port.in;

import java.time.LocalDate;

public interface ResolveEffectiveTripOriginUseCase {
    RouteOriginResolution resolve(LocalDate travelDate, String scheduleBlock);
}
