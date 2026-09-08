package com.lunaris.ansenuza.application.conversation;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;

public final class BotRoute {
    private BotRoute() {}

    public static boolean fromCordoba(String locality) {
        return locality != null && Normalizer.normalize(locality.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").equalsIgnoreCase("Cordoba");
    }

    public static List<Locality> destinations(LocalityRepository repository) {
        return repository.findAllWithActiveFare().stream()
                .filter(locality -> !fromCordoba(locality.getName()))
                .sorted(Comparator.comparing(Locality::getName))
                .toList();
    }
}
