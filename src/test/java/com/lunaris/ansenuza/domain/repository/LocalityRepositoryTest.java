package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:locality-repository;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class LocalityRepositoryTest {

    @Autowired
    private LocalityRepository localityRepository;

    @Autowired
    private FareRepository fareRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFindAllWithActiveFare_FiltersZeroAndNullAmounts() {
        localityRepository.saveAll(List.of(
                Locality.builder().name("Activa").build(),
                Locality.builder().name("Tarifa cero").build(),
                Locality.builder().name("Tarifa nula").build()));
        fareRepository.saveAll(List.of(
                Fare.builder().localityName("Activa").amount(new BigDecimal("1000")).build(),
                Fare.builder().localityName("Tarifa cero").amount(BigDecimal.ZERO).build()));
        jdbcTemplate.execute("ALTER TABLE fares ALTER COLUMN amount DROP NOT NULL");
        jdbcTemplate.update(
                "INSERT INTO fares (id, locality_name, amount) VALUES (RANDOM_UUID(), ?, NULL)",
                "Tarifa nula");

        assertThat(localityRepository.findAllWithActiveFare())
                .extracting(Locality::getName)
                .containsExactly("Activa");
    }

    @Test
    void testExactNameMatch_DistinguishesSimilarPrefixes() {
        localityRepository.saveAll(List.of(
                Locality.builder().name("San Francisco").build(),
                Locality.builder().name("San Guillermo").build()));

        assertThat(localityRepository.findFirstByNameIgnoreCase("san francisco"))
                .get()
                .extracting(Locality::getName)
                .isEqualTo("San Francisco");
        assertThat(localityRepository.findFirstByNameIgnoreCase("san guillermo"))
                .get()
                .extracting(Locality::getName)
                .isEqualTo("San Guillermo");
    }
}
