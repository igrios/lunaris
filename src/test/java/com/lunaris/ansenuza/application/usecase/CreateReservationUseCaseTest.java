package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

class CreateReservationUseCaseTest {

    @Test
    void executeDelegatesAmountCalculationToPricingService() throws Exception {
        UUID passengerId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .id(passengerId)
                .firstName("Juan")
                .lastName("Perez")
                .phone("1234567890")
                .build();

        Object passengerRepository = proxy(
                Class.forName("com.lunaris.ansenuza.domain.repository.PassengerRepository"),
                (method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(passenger);
                    }
                    return defaultValue(method.getReturnType());
                });

        Object pricingService = newPricingService();
        ReservationServiceFixture reservationFixture = newReservationService();
        Object useCase = newUseCase(passengerRepository, pricingService, reservationFixture.service);

        Object request = newRequest(
                passengerId,
                LocalDate.of(2026, 6, 30),
                "Morteros",
                "Av. San Martín 123",
                "Córdoba",
                false,
                null,
                false,
                "nota",
                2,
                "Ana, Luis");

        Method execute = Class.forName("com.lunaris.ansenuza.application.usecase.CreateReservationUseCase")
                .getMethod("execute",
                        Class.forName("com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest"));
        Reservation result = (Reservation) execute.invoke(useCase, request);
        Reservation persisted = reservationFixture.savedReservation.get();

        assertSame(persisted, result);
        assertEquals(new BigDecimal("116000.00"), persisted.getAmount());
        assertEquals(2, persisted.getPassengerCount());
        assertEquals("PENDING_PAYMENT", persisted.getStatus());
        assertSame(passenger, persisted.getPassenger());
    }

    private Object newUseCase(Object passengerRepository, Object pricingService, Object reservationService)
            throws Exception {
        Object useCase = allocate(Class.forName("com.lunaris.ansenuza.application.usecase.CreateReservationUseCase"));
        setField(useCase, "passengerRepository", passengerRepository);
        setField(useCase, "pricingAndScheduleService", pricingService);
        setField(useCase, "reservationService", reservationService);
        return useCase;
    }

    private Object newPricingService() throws Exception {
        Object pricingService = allocate(Class.forName("com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService"));

        Object fareRepository = proxy(
                Class.forName("com.lunaris.ansenuza.domain.repository.FareRepository"),
                (method, args) -> {
                    if ("findByLocalityNameIgnoreCase".equals(method.getName())) {
                        return Optional.of(com.lunaris.ansenuza.domain.model.Fare.builder()
                                .localityName("Morteros")
                                .amount(new BigDecimal("100000"))
                                .build());
                    }
                    return defaultValue(method.getReturnType());
                });
        setField(pricingService, "fareRepository", fareRepository);
        setField(pricingService, "localityRepository",
                proxy(Class.forName("com.lunaris.ansenuza.domain.repository.LocalityRepository"),
                        (method, args) -> defaultValue(method.getReturnType())));
        setField(pricingService, "businessParameterRepository",
                proxy(Class.forName("com.lunaris.ansenuza.domain.repository.BusinessParameterRepository"),
                        (method, args) -> defaultValue(method.getReturnType())));
        setField(pricingService, "reservationRepository",
                proxy(Class.forName("com.lunaris.ansenuza.domain.repository.ReservationRepository"),
                        (method, args) -> defaultValue(method.getReturnType())));
        return pricingService;
    }

    private ReservationServiceFixture newReservationService() throws Exception {
        Object reservationService = allocate(Class.forName("com.lunaris.ansenuza.domain.model.service.ReservationService"));
        AtomicReference<Reservation> savedReservation = new AtomicReference<>();

        Object reservationRepository = proxy(
                Class.forName("com.lunaris.ansenuza.domain.repository.ReservationRepository"),
                (method, args) -> {
                    if ("countSequenceByRouteAndDate".equals(method.getName())) return 0L;
                    if ("existsByReservationCode".equals(method.getName())) return false;
                    if ("save".equals(method.getName()) || "saveAndFlush".equals(method.getName())) {
                        savedReservation.set((Reservation) args[0]);
                        return args[0];
                    }
                    if ("findById".equals(method.getName())) return Optional.empty();
                    return defaultValue(method.getReturnType());
        });
        setField(reservationService, "reservationRepository", reservationRepository);
        setField(reservationService, "reservationEventRepository",
                proxy(Class.forName("com.lunaris.ansenuza.domain.repository.ReservationEventRepository"),
                        (method, args) -> defaultValue(method.getReturnType())));
        return new ReservationServiceFixture(reservationService, savedReservation);
    }

    private Object newRequest(
            UUID passengerId,
            LocalDate travelDate,
            String pickupLocality,
            String pickupAddress,
            String destination,
            Boolean roundTrip,
            LocalDate returnDate,
            Boolean paymentVerified,
            String notes,
            Integer passengerCount,
            String companionNames) throws Exception {

        Class<?> requestClass = Class.forName(
                "com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest");
        Constructor<?> constructor = requestClass.getDeclaredConstructors()[0];
        return constructor.newInstance(
                passengerId,
                travelDate,
                pickupLocality,
                pickupAddress,
                destination,
                roundTrip,
                returnDate,
                paymentVerified,
                notes,
                passengerCount,
                companionNames);
    }

    private Object allocate(Class<?> type) throws Exception {
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocateInstance = unsafe.getClass().getMethod("allocateInstance", Class.class);
        return allocateInstance.invoke(unsafe, type);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object proxy(Class<?> interfaceType, BiMethod body) {
        return Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[] {interfaceType},
                (proxy, method, args) -> body.apply(method, args));
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }

    @FunctionalInterface
    private interface BiMethod {
        Object apply(Method method, Object[] args) throws Throwable;
    }

    private static final class ReservationServiceFixture {
        final Object service;
        final AtomicReference<Reservation> savedReservation;

        ReservationServiceFixture(Object service, AtomicReference<Reservation> savedReservation) {
            this.service = service;
            this.savedReservation = savedReservation;
        }
    }
}
