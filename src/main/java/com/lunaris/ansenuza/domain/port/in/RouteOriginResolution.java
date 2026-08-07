package com.lunaris.ansenuza.domain.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RouteOriginResolution(
        LocalDate travelDate,
        String scheduleBlock,
        String effectiveOrigin,
        List<String> skippedLocalities,
        Map<String, Integer> minuteOffsets,
        String summary) {
}
