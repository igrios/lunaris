package com.lunaris.ansenuza.application.conversation;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class GoogleMapsParameterFormatter {

    private GoogleMapsParameterFormatter() {
    }

    public static String encode(String location) {
        String normalized = normalize(location);
        return URLEncoder.encode(normalized, StandardCharsets.UTF_8)
                .replace("%2C", ",");
    }

    static String normalize(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String trimmed = location.trim();
        try {
            URI uri = URI.create(trimmed);
            if (uri.getHost() == null || !uri.getHost().endsWith("google.com")) {
                return trimmed;
            }
            return Arrays.stream(uri.getRawQuery() == null ? new String[0] : uri.getRawQuery().split("&"))
                    .map(parameter -> parameter.split("=", 2))
                    .filter(parts -> parts.length == 2 && "q".equals(parts[0]))
                    .map(parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8))
                    .findFirst()
                    .orElse("");
        } catch (IllegalArgumentException ignored) {
            return trimmed;
        }
    }
}
