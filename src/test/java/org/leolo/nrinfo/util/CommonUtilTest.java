package org.leolo.nrinfo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CommonUtilTest {


    @Test
    public void testToCamelCase() {
        assertEquals("Lorem Ipsum Dolor Sit Amet", CommonUtil.toCamelCase("Lorem Ipsum dolor sit amet"));
        assertEquals("Lorem Ipsum Dolor (Sit Amet)", CommonUtil.toCamelCase("Lorem Ipsum dolor (sit amet)"));
        assertNull(CommonUtil.toCamelCase(null));
    }

    @ParameterizedTest
    @MethodSource("trueValues")
    void parseBoolean_shouldReturnTrueForTrueValues(String value) {
        assertEquals(true, CommonUtil.parseBoolean(value, false));
    }

    private static Stream<Arguments> trueValues() {
        return Stream.of(
                Arguments.of("true"),
                Arguments.of("TRUE"),
                Arguments.of("True"),
                Arguments.of("yes"),
                Arguments.of("YES"),
                Arguments.of("y"),
                Arguments.of("Y"),
                Arguments.of("on"),
                Arguments.of("ON"),
                Arguments.of("1")
        );
    }

    @ParameterizedTest
    @MethodSource("falseValues")
    void parseBoolean_shouldReturnFalseForFalseValues(String value) {
        assertEquals(false, CommonUtil.parseBoolean(value, true));
    }

    private static Stream<Arguments> falseValues() {
        return Stream.of(
                Arguments.of("false"),
                Arguments.of("FALSE"),
                Arguments.of("False"),
                Arguments.of("no"),
                Arguments.of("NO"),
                Arguments.of("n"),
                Arguments.of("N"),
                Arguments.of("off"),
                Arguments.of("OFF"),
                Arguments.of("0")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void parseBoolean_shouldReturnDefaultForInvalidValues(String value) {
        assertTrue(CommonUtil.parseBoolean(value, true));
        assertFalse(CommonUtil.parseBoolean(value, false));
    }

    private static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("maybe"),
                Arguments.of("TRUEE"),
                Arguments.of("10"),
                Arguments.of("2"),
                Arguments.of("yes "),
                Arguments.of(" yes"),
                Arguments.of(" true ")
        );
    }
}
