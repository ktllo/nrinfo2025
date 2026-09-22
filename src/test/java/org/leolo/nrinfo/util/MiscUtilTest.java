package org.leolo.nrinfo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MiscUtilTest {


    @Test
    void testParseNaptanDate_NullOrEmpty() throws ParseException {
        assertNull(MiscUtil.parseNaptanDate(null), "Should return null for null input");
        assertNull(MiscUtil.parseNaptanDate(""), "Should return null for empty string");
    }

    @Test
    void testParseNaptanDate_Valid23Chars() throws ParseException {
        String dateStr = "2023-10-27T10:15:30.500";
        Date result = MiscUtil.parseNaptanDate(dateStr);

        assertNotNull(result);

        // Verify specific components to ensure milliseconds were parsed correctly
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(27, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void testParseNaptanDate_Valid19Chars() throws ParseException {
        String dateStr = "2023-10-27T10:15:30";
        Date result = MiscUtil.parseNaptanDate(dateStr);

        assertNotNull(result);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND)); // No millis provided
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2023-10-27",                // Too short (10)
            "2023-10-27T10:15:30.5",     // Wrong millis precision (21)
            "2023-10-27T10:15:30.5000",  // Too long (24)
            "not-a-date-at-all-length-19" // Correct length, wrong content
    })
    void testParseNaptanDate_InvalidLengthOrFormat(String invalidDate) {
        assertThrows(ParseException.class, () -> {
            MiscUtil.parseNaptanDate(invalidDate);
        }, "Should throw ParseException for: " + invalidDate);
    }

    @Test
    void testParseNaptanDate_InvalidData() {
        // This test checks that the method at least attempts to parse
        // or throws if the structure is broken.
        assertThrows(ParseException.class, () -> {
            MiscUtil.parseNaptanDate("invalid-format-19ch");
        });
    }

    @Test
    public void testMin() {
        assertEquals("A", MiscUtil.min("A", "B", "C"));
        assertEquals("A", MiscUtil.min("C", "B", "A"));
        assertEquals("A", MiscUtil.min(null, "C", "B", "A"));
        assertEquals("A", MiscUtil.min("C", "B", null, "A"));
        assertEquals("A", MiscUtil.min("C", "B", "A", null));
        assertEquals(10, MiscUtil.min(45, 23, 10));
        assertEquals("A", MiscUtil.min("A"));
        assertNull(MiscUtil.min(null, null, null));
        assertNull(MiscUtil.min(null));
        assertNull(MiscUtil.min());
        assertNull(MiscUtil.min(new String[0]));
    }

    @Test
    public void testMax() {
        assertEquals("C", MiscUtil.max("A", "B", "C"));
        assertEquals("C", MiscUtil.max("C", "B", "A"));
        assertEquals("C", MiscUtil.max(null, "C", "B", "A"));
        assertEquals("C", MiscUtil.max("C", "B", null, "A"));
        assertEquals("C", MiscUtil.max("C", "B", "A", null));
        assertEquals("A", MiscUtil.max("A"));
        assertEquals(45, MiscUtil.max(45, 23, 10));
        assertNull(MiscUtil.max(null, null, null));
        assertNull(MiscUtil.max(null));
        assertNull(MiscUtil.max());
        assertNull(MiscUtil.max(new String[0]));
    }

    @Test
    public void testFormatList() {
        assertEquals("A, B, C, D, E and F", MiscUtil.formatList(List.of("A", "B", "C", "D", "E", "F"), ", ", " and "));
        assertEquals("A, B, C, D, E or F", MiscUtil.formatList(List.of("A", "B", "C", "D", "E", "F"), ", ", " or "));
        assertEquals("A,B,C,D,E and F", MiscUtil.formatList(List.of("A", "B", "C", "D", "E", "F"), ",", " and "));
        assertEquals("", MiscUtil.formatList(new ArrayList<String>(), ", ", " and "));
        assertEquals("", MiscUtil.formatList(null, ", ", " and "));
        assertEquals("A", MiscUtil.formatList(List.of("A"), ", ", " and "));
        assertEquals("A and B", MiscUtil.formatList(List.of("A", "B"), ", ", " and "));
        assertEquals("A, B and C", MiscUtil.formatList(List.of("A", "B", "C"), ", ", " and "));
        assertEquals(", B and C", MiscUtil.formatList(List.of("", "B", "C"), ", ", " and "));
        assertEquals("A,  and C", MiscUtil.formatList(List.of("A", "", "C"), ", ", " and "));
        assertThrows(NullPointerException.class, () -> MiscUtil.formatList(List.of("A", null, "C"), ", ", " and "));

    }
}
