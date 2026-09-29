package com.lunaris.ansenuza.service.interurban;

import java.util.UUID;
import org.springframework.security.core.Authentication;

public interface DriverIdentityPort {
    UUID requireDriver(Authentication authentication);
}
