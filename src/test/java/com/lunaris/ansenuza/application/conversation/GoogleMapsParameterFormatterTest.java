package com.lunaris.ansenuza.application.conversation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GoogleMapsParameterFormatterTest {

    @Test
    void extractsRawCoordinatesFromGoogleMapsQueryUrl() {
        assertEquals("-31.4285262,-64.1663274",
                GoogleMapsParameterFormatter.encode(
                        "https://maps.google.com/?q=-31.4285262,-64.1663274"));
    }

    @Test
    void preservesAndEncodesRawStreetAddress() {
        assertEquals("Av.+San+Mart%C3%ADn+450,+Morteros",
                GoogleMapsParameterFormatter.encode("Av. San Martín 450, Morteros"));
    }

    @Test
    void decodesCoordinatesBeforeFormattingThem() {
        assertEquals("-31.4285262,-64.1663274",
                GoogleMapsParameterFormatter.encode(
                        "https://www.google.com/maps?q=-31.4285262%2C-64.1663274"));
    }

    @Test
    void neverPassesAFullGoogleMapsUrlAsDirectionsParameter() {
        assertEquals("", GoogleMapsParameterFormatter.encode("https://maps.google.com/maps"));
    }
}
