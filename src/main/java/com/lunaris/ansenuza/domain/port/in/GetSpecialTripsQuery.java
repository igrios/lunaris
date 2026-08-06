package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import java.util.List;

public interface GetSpecialTripsQuery {
    List<SpecialTrip> getAll();
    List<SpecialTrip> getActive();
}
