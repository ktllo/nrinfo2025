package org.leolo.nrinfo.job;

import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SchedulePruneJob extends AbstractJob{

    private Logger logger = LoggerFactory.getLogger(SchedulePruneJob.class);

    @Autowired private ConfigurationService configurationService;
    @Autowired private ScheduleService scheduleService;


    @Override
    public void run() {
        logger.info("SchedulePruneJob started");
        int retainPeriod = configurationService.getInt("schedule.retain_period", 7);
        Instant cutOff = Instant.now().minus(retainPeriod, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        logger.info("Cut off is {}", cutOff);
        scheduleService.pruneSchedule(cutOff);
        logger.info("SchedulePruneJob finished");
    }
}
