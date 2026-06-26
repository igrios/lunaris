package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.model.Fare;

class PricingAndScheduleServiceTest {

    @Test
    void calculateTripPriceAppliesOneWayRuleAndSeatCount() throws Exception {
        Object service = newService();

        BigDecimal amount = (BigDecimal) invoke(
                service,
                "calculateTripPrice",
                new Class<?>[] {String.class, Boolean.class, int.class},
                "Morteros",
                false,
                2);

        assertEquals(new BigDecimal("116000.00"), amount);
    }

    @Test
    void calculateReservationAmountUsesZoneLocalityAndSamePricingRule() throws Exception {
        Object service = newService();

        BigDecimal amount = (BigDecimal) invoke(
                service,
                "calculateReservationAmount",
                new Class<?>[] {String.class, String.class, Boolean.class, int.class},
                "Córdoba",
                "Morteros",
                false,
                1);

        assertEquals(new BigDecimal("58000.00"), amount);
    }

    private Object newService() throws Exception {
        Class<?> fareRepositoryClass = Class.forName(
                "com.lunaris.ansenuza.domain.repository.FareRepository");
        Class<?> localityRepositoryClass = Class.forName(
                "com.lunaris.ansenuza.domain.repository.LocalityRepository");
        Class<?> businessParameterRepositoryClass = Class.forName(
                "com.lunaris.ansenuza.domain.repository.BusinessParameterRepository");
        Class<?> reservationRepositoryClass = Class.forName(
                "com.lunaris.ansenuza.domain.repository.ReservationRepository");

        Object service = allocate(Class.forName(
                "com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService"));

        Fare fare = Fare.builder()
                .localityName("Morteros")
                .amount(new BigDecimal("100000"))
                .build();

        setField(service, "fareRepository", proxy(fareRepositoryClass, (method, args) -> {
            if ("findByLocalityNameIgnoreCase".equals(method.getName())) {
                return Optional.of(fare);
            }
            return defaultValue(method.getReturnType());
        }));
        setField(service, "localityRepository",
                proxy(localityRepositoryClass, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "businessParameterRepository",
                proxy(businessParameterRepositoryClass, (method, args) -> defaultValue(method.getReturnType())));
        setField(service, "reservationRepository",
                proxy(reservationRepositoryClass, (method, args) -> defaultValue(method.getReturnType())));

        return service;
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        return method.invoke(target, args);
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
}
