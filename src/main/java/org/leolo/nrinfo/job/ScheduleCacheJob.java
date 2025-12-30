package org.leolo.nrinfo.job;

import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Component
@Scope("prototype")
public class ScheduleCacheJob extends AbstractJob {

    private Logger logger = LoggerFactory.getLogger(ScheduleCacheJob.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ScheduleService scheduleService;


    @Override
    public void run() {
        logger.info("Starting ScheduleCacheJob");
        Instant baseDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
        int day_gen = configurationService.getInt("networkrail.schedule_cache", 7);
        for (int i = 1; i <= day_gen; i++) {
            Instant date = baseDate.plus(i, ChronoUnit.DAYS);
            logger.debug("Caching trains for {}", date);
            scheduleService.buildScheduleCache(date);
        }
    }
}
