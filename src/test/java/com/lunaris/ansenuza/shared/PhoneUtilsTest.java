package com.lunaris.ansenuza.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PhoneUtilsTest {

    @ParameterizedTest
    @MethodSource("phoneFormats")
    void normalizesArgentinePhonesWithoutMobileNine(String rawPhone) {
        assertEquals("543512282251", PhoneUtils.normalizeArgentinePhone(rawPhone));
    }

    private static Stream<Arguments> phoneFormats() {
        return Stream.of(
                arguments("3512282251"),
                arguments("03512282251"),
                arguments("+5493512282251"),
                arguments("+54 9 351-228-2251"),
                arguments("0351 15 2282251"));
    }
}
