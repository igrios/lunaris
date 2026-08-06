package com.lunaris.ansenuza.domain.port.in;

import java.util.UUID;

public interface DeleteFareLocalityUseCase {
    void delete(UUID fareId);
}
