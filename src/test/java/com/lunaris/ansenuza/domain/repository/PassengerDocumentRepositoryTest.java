package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Passenger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class PassengerDocumentRepositoryTest {
    @Autowired PassengerRepository repository;

    @Test
    void matchesFormattedDocumentsAndLimitsDuplicateResults() {
        repository.saveAndFlush(Passenger.builder().firstName("Ana").lastName("Pérez")
                .cuil("20-12345678-9").build());
        repository.saveAndFlush(Passenger.builder().firstName("Otra").lastName("Pérez")
                .cuil("20 12345678 9").build());
        repository.saveAndFlush(Passenger.builder().firstName("Juan").lastName("Pérez")
                .cuil("12.345.678").build());
        var page = PageRequest.of(0, 1);
        assertEquals(1, repository.findByDocument("20123456789", page).size());
        assertEquals("Juan", repository.findByDocument("12345678", page).getFirst().getFirstName());
        assertTrue(repository.findByDocument("99999999", page).isEmpty());
    }
}
