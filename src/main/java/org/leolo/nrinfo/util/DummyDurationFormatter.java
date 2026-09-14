package org.leolo.nrinfo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DummyDurationFormatter {

    private static Logger log = LoggerFactory.getLogger(DummyDurationFormatter.class);

    @Deprecated
    public String format(Duration duration) {
        return String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    public String format(Duration duration, Date baseDate) {
        if (duration == null) {
            return null;
        }
        if (baseDate == null) {
            return format(duration);
        } else if (baseDate instanceof java.sql.Date) {
            //This subclass does not permit convert to Instant
            baseDate = new Date(baseDate.getTime());
        }
//        log.debug("BaseDate: {}, time: {}", baseDate, duration);
        if (duration.compareTo(Duration.ofDays(1)) >= 0) {
            log.debug("ADDDAY - BaseDate: {}, time: {}", baseDate, duration);
            //It is actually on next day
            baseDate = Date.from(baseDate.toInstant().plus(1, ChronoUnit.DAYS));
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(baseDate)+
                String.format(" %02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
