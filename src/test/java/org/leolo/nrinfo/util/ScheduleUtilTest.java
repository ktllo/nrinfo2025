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
        assertNull(ScheduleUtil.parseAllowance("  "));
        assertNull(ScheduleUtil.parseAllowance("   "));
    }

    @Test void testFormatDaysRunsMasks(){
        //Commonly seen
        assertEquals("SSuX",ScheduleUtil.formatDaysRunsMasks("1111100"));
        assertEquals("SuX",ScheduleUtil.formatDaysRunsMasks("1111110"));
        assertEquals("SuO",ScheduleUtil.formatDaysRunsMasks("0000001"));
        assertEquals("MO",ScheduleUtil.formatDaysRunsMasks("1000000"));

        //Additional Case
        assertEquals("MTuWO",ScheduleUtil.formatDaysRunsMasks("1110000"));
        assertEquals("ThFSO",ScheduleUtil.formatDaysRunsMasks("0001110"));

        //Error case
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.formatDaysRunsMasks(""));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.formatDaysRunsMasks(null));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.formatDaysRunsMasks("00000000"));
        assertThrows(IllegalArgumentException.class, () -> ScheduleUtil.formatDaysRunsMasks("000000"));
    }

}
