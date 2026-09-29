package com.lunaris.ansenuza.service.interurban;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.driver.enabled"}, havingValue = "true")
public class InterurbanDriverController {
    public record CheckInRequest(String token, UUID tripId) {
        @Override public String toString() { return "CheckInRequest[token=REDACTED,tripId=" + tripId + "]"; }
    }
    private final DriverRouteSheetQuery routes;
    private final CheckInService checkIn;

    public InterurbanDriverController(DriverRouteSheetQuery routes, CheckInService checkIn) {
        this.routes = routes;
        this.checkIn = checkIn;
    }

    @GetMapping("/api/v1/driver/route-sheet")
    public ResponseEntity<DriverRouteSheetQuery.RouteSheet> routeSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication, CsrfToken csrf) {
        var sheet = routes.find(date, authentication);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(csrf.getHeaderName(), csrf.getToken()).body(sheet);
    }

    @PostMapping("/api/v1/checkin/verify")
    public ResponseEntity<CheckInService.Result> verify(@RequestBody CheckInRequest request, Authentication authentication) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(checkIn.verify(request.token(), request.tripId(), authentication));
    }
}
