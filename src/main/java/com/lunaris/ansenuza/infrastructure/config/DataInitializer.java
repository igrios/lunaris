package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.BusinessParameter;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class DataInitializer implements CommandLineRunner {

    private static final List<LocalitySeed> DEFAULT_LOCALITIES = List.of(
            new LocalitySeed("Córdoba Capital", 0, 0, "20000"),
            new LocalitySeed("Río Primero", 52, 45, "30000"),
            new LocalitySeed("Villa Santa Rosa", 86, 75, "38000"),
            new LocalitySeed("Obispo Trejo", 122, 105, "44000"),
            new LocalitySeed("La Puerta", 128, 115, "46000"),
            new LocalitySeed("La Para", 148, 135, "50000"),
            new LocalitySeed("Marull", 167, 155, "54000"),
            new LocalitySeed("Balnearia", 180, 170, "56000"),
            new LocalitySeed("Miramar", 197, 185, "62000"));

    private static final Map<String, String> DEFAULT_BUSINESS_PARAMETERS = Map.of(
            "ONE_WAY_EXTRA_AMOUNT", "8000",
            "PRICE_PER_KM", "1000");

    private static final Map<String, String> DEFAULT_SYSTEM_CONFIGURATIONS = Map.of(
            "return.scheduler.time", "15:00",
            "return.message.header", "Confirmación de vuelta",
            "return.message.body",
                    "Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?\n"
                            + "Elegí una opción para que podamos organizar las butacas.",
            "return.button.yes.title", "SÍ, VOLVER ✅",
            "return.button.later.title", "OTRO DÍA 📅",
            "return.button.no.title", "NO, CANCELAR ❌",
            "session.inactivity.timeout.minutes", "30");

    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;
    private final BusinessParameterRepository businessParameterRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    public DataInitializer(
            LocalityRepository localityRepository,
            FareRepository fareRepository,
            BusinessParameterRepository businessParameterRepository,
            SystemConfigurationRepository systemConfigurationRepository) {
        this.localityRepository = localityRepository;
        this.fareRepository = fareRepository;
        this.businessParameterRepository = businessParameterRepository;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (localityRepository.count() == 0 && fareRepository.count() == 0) {
            seedLocalitiesAndFares();
        }
        seedBusinessParameters();
        seedSystemConfigurations();
    }

    private void seedLocalitiesAndFares() {
        List<Locality> localities = DEFAULT_LOCALITIES.stream()
                .map(seed -> Locality.builder()
                        .id(UUID.randomUUID())
                        .name(seed.name())
                        .kmsToCordoba(seed.kmsToCordoba())
                        .minutesFromOrigin(seed.minutesFromOrigin())
                        .build())
                .toList();
        localityRepository.saveAllAndFlush(localities);

        List<Fare> fares = DEFAULT_LOCALITIES.stream()
                .map(seed -> Fare.builder()
                        .id(UUID.randomUUID())
                        .localityName(seed.name())
                        .amount(new BigDecimal(seed.amount()))
                        .build())
                .toList();
        fareRepository.saveAllAndFlush(fares);
    }

    private void seedBusinessParameters() {
        DEFAULT_BUSINESS_PARAMETERS.forEach((key, value) -> {
            if (!businessParameterRepository.existsById(key)) {
                businessParameterRepository.save(BusinessParameter.builder()
                        .parameterKey(key)
                        .parameterValue(value)
                        .build());
            }
        });
    }

    private void seedSystemConfigurations() {
        DEFAULT_SYSTEM_CONFIGURATIONS.forEach((key, value) -> {
            if (!systemConfigurationRepository.existsById(key)) {
                systemConfigurationRepository.save(SystemConfiguration.builder()
                        .key(key)
                        .value(value)
                        .build());
            }
        });
    }

    private record LocalitySeed(
            String name, int kmsToCordoba, int minutesFromOrigin, String amount) {
    }
}
