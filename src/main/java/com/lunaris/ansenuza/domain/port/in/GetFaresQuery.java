package com.lunaris.ansenuza.domain.port.in;

import java.util.List;

public interface GetFaresQuery {
    List<FareLocalityView> getAll();
}
