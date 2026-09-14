package org.leolo.nrinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.util.ScheduleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.sql.Time;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
public class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @Test
    public void testProcessCrossDaySchedule_main() {
        Schedule schedule = new Schedule();
        schedule.setTrainUid("TEST01");
        ScheduleDetail sd = new ScheduleDetail();
        //WRGTNBQ 23:59:30 > BUS
        sd.setLocation("WRGTNBQ");
        sd.setLocationInstance(0);
        sd.setDepartureTime(ScheduleUtil.parseDuration("2359H"));
        sd.setLine("BUS");
        sd.setPublicDepartureTime(ScheduleUtil.parseDuration("2359"));
        schedule.getDetailList().add(sd);
        //ERLSTWN 00:24:30
        sd = new ScheduleDetail();
        sd.setLocation("ERLSTWN");
        sd.setLocationInstance(0);
        sd.setDepartureTime(ScheduleUtil.parseDuration("0024H"));
        sd.setArrivalTime(ScheduleUtil.parseDuration("0024H"));
        sd.setPublicDepartureTime(ScheduleUtil.parseDuration("0024"));
        sd.setPublicArrivalTime(ScheduleUtil.parseDuration("0024"));
        schedule.getDetailList().add(sd);
        //NWTNLW 00:34:30
        sd = new ScheduleDetail();
        sd.setLocation("NWTNLW");
        sd.setLocationInstance(0);
        sd.setDepartureTime(ScheduleUtil.parseDuration("0034H"));
        sd.setArrivalTime(ScheduleUtil.parseDuration("0034H"));
        sd.setPublicDepartureTime(ScheduleUtil.parseDuration("0034"));
        sd.setPublicArrivalTime(ScheduleUtil.parseDuration("0034"));
        schedule.getDetailList().add(sd);
        //MNCRPIC BUS > 01:09:30
        sd = new ScheduleDetail();
        sd.setLocation("MNCRPIC");
        sd.setLocationInstance(0);
        sd.setArrivalTime(ScheduleUtil.parseDuration("0109H"));
        sd.setPublicArrivalTime(ScheduleUtil.parseDuration("0110"));
        sd.setPath("BUS");
        schedule.getDetailList().add(sd);
        scheduleService.processCrossDaySchedule(schedule);
        //assert
        //WRGTNBQ 23:59:30 > BUS
        sd = schedule.getDetailList().get(0);
        assertNull(sd.getArrivalTime());
        assertNull(sd.getPublicArrivalTime());
        assertNull(sd.getPassTime());
        assertEquals(ScheduleUtil.parseDuration("2359H"), sd.getDepartureTime());
        assertEquals(ScheduleUtil.parseDuration("2359"), sd.getPublicDepartureTime());
        //ERLSTWN 00:24:30+1
        sd = schedule.getDetailList().get(1);
        assertEquals(ScheduleUtil.parseDuration("2424H"), sd.getArrivalTime());
        assertEquals(ScheduleUtil.parseDuration("2424"), sd.getPublicArrivalTime());
        assertNull(sd.getPassTime());
        assertEquals(ScheduleUtil.parseDuration("2424H"), sd.getDepartureTime());
        assertEquals(ScheduleUtil.parseDuration("2424"), sd.getPublicDepartureTime());
        //NWTNLW 00:34:30+1
        sd = schedule.getDetailList().get(2);
        assertEquals(ScheduleUtil.parseDuration("2434H"), sd.getArrivalTime());
        assertEquals(ScheduleUtil.parseDuration("2434"), sd.getPublicArrivalTime());
        assertNull(sd.getPassTime());
        assertEquals(ScheduleUtil.parseDuration("2434H"), sd.getDepartureTime());
        assertEquals(ScheduleUtil.parseDuration("2434"), sd.getPublicDepartureTime());
        //MNCRPIC BUS > 01:09:30+1
        sd = schedule.getDetailList().get(3);
        assertEquals(ScheduleUtil.parseDuration("2509H"), sd.getArrivalTime());
        assertEquals(ScheduleUtil.parseDuration("2510"), sd.getPublicArrivalTime());
        assertNull(sd.getPassTime());
        assertNull(sd.getDepartureTime());
        assertNull(sd.getPublicDepartureTime());
    }

    @Test
    public void testAddDay() throws Exception {
//        assertNull(scheduleService.addDay(null));
        Duration t = ScheduleUtil.parseDuration("2359");
        Duration at = scheduleService.addDay(t);
        Duration d = at.minus(t);
        assertEquals(Duration.ofDays(1), d);
    }
}
