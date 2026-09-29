package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import java.util.stream.IntStream;

public record Route(int origin, int destination) {
    public Route {
        if (origin < 0 || origin > 3 || destination < 0 || destination > 3 || origin == destination) {
            throw new InvalidRouteException("Origen y destino deben ser paradas distintas entre 0 y 3.");
        }
    }

    public List<Integer> ordinals() {
        return IntStream.rangeClosed(Math.min(origin, destination) + 1,
                Math.max(origin, destination)).boxed().toList();
    }

    public int direction() { return Integer.signum(destination - origin); }
}
