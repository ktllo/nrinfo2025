package org.leolo.nrinfo.util;

import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleUtilTest {

    @Test
    public void testParsingTime() {
        Time time = ScheduleUtil.parseTime("2012");
        assertEquals(Time.valueOf("20:12:00"), time);
        time = ScheduleUtil.parseTime("2012H");
        assertEquals(Time.valueOf("20:12:30"), time);
        time = ScheduleUtil.parseTime("2012 ");
        assertEquals(Time.valueOf("20:12:00"), time);
    }

    @Test void testParsingAllowance() {
        Time time = null;
        time = ScheduleUtil.parseAllowance(" H");
        assertEquals(Time.valueOf("00:00:30"), time);
        time = ScheduleUtil.parseAllowance("H ");
        assertEquals(Time.valueOf("00:00:30"), time);
        time = ScheduleUtil.parseAllowance("1 ");
        assertEquals(Time.valueOf("00:01:00"), time);
        time = ScheduleUtil.parseAllowance(" 1");
        assertEquals(Time.valueOf("00:01:00"), time);
        time = ScheduleUtil.parseAllowance("1H");
        assertEquals(Time.valueOf("00:01:30"), time);
        time = ScheduleUtil.parseAllowance("15");
        assertEquals(Time.valueOf("00:15:00"), time);
    }

    @Test void testParseTimeError() {
        assertNull(ScheduleUtil.parseTime(""));
        assertNull(ScheduleUtil.parseTime(null));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseTime(" "));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseTime("    "));//4 space
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseTime("     "));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseTime("LOREM"));
    }

    @Test void testParseAllowanceError() {
        assertNull(ScheduleUtil.parseAllowance(""));
        assertNull(ScheduleUtil.parseAllowance(null));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseAllowance("  "));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.parseAllowance("   "));
    }

}
