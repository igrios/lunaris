package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

class ExistingDriverIdentityAdapterTest {
    Driver driver(String phone) {
        var driver = new Driver();
        driver.setId(UUID.randomUUID()); driver.setPhone(phone); driver.setActive(true);
        return driver;
    }

    @Test void normalizesAccountAndDriverPhones() {
        var repository = mock(DriverRepository.class);
        var driver = driver("+54 9 3562 123456");
        when(repository.findByActiveTrue()).thenReturn(List.of(driver));
        var auth = new UsernamePasswordAuthenticationToken("543562123456", "", AuthorityUtils.createAuthorityList("ROLE_CHOFER"));
        assertEquals(driver.getId(), new ExistingDriverIdentityAdapter(repository).requireDriver(auth));
    }

    @Test void ambiguousIdentityFailsClosed() {
        var repository = mock(DriverRepository.class);
        when(repository.findByActiveTrue()).thenReturn(List.of(driver("5493562123456"), driver("543562123456")));
        var auth = new UsernamePasswordAuthenticationToken("543562123456", "", AuthorityUtils.createAuthorityList("ROLE_CHOFER"));
        assertThrows(AccessDeniedException.class, () -> new ExistingDriverIdentityAdapter(repository).requireDriver(auth));
    }

    @Test void passengerCannotResolveDriverEvenWithMatchingPhone() {
        var repository = mock(DriverRepository.class);
        var auth = new UsernamePasswordAuthenticationToken("543562123456", "", AuthorityUtils.createAuthorityList("ROLE_PASSENGER"));
        assertThrows(AccessDeniedException.class, () -> new ExistingDriverIdentityAdapter(repository).requireDriver(auth));
        verifyNoInteractions(repository);
    }
}
