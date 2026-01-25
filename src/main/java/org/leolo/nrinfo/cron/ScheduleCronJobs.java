package org.leolo.nrinfo.cron;

import org.leolo.nrinfo.job.NetworkRailScheduleImportJob;
import org.leolo.nrinfo.job.ScheduleCacheJob;
import org.leolo.nrinfo.job.SchedulePruneJob;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ScheduleCronJobs {

    private Logger log = LoggerFactory.getLogger(ScheduleCronJobs.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JobService jobService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigurationService configurationService;

    @Scheduled(cron = "0 40 3 * * *")
    public void scheduleCronJobs() {
        NetworkRailScheduleImportJob job = applicationContext.getBean(NetworkRailScheduleImportJob.class);
        job.setJobOwner(userService.getUserByUsername(configurationService.getString("system_job_runner")).getUserId());
        String url = String.format(NetworkRailScheduleImportJob.DIFF_URL, new SimpleDateFormat("EEE").format(new Date()).toLowerCase());
        log.info("Job url: {}", url);
        job.setUrl(url);
        jobService.queueJob(job);
    }

    @Scheduled(cron = "30 0 5 * * *")
    public void buildCache(){
        ScheduleCacheJob job = applicationContext.getBean(ScheduleCacheJob.class);
        job.setJobOwner(userService.getUserByUsername(configurationService.getString("system_job_runner")).getUserId());
        jobService.queueJob(job);
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void pruneSchedule() {
        SchedulePruneJob job = applicationContext.getBean(SchedulePruneJob.class);
        job.setJobOwner(userService.getUserByUsername(configurationService.getString("system_job_runner")).getUserId());
        jobService.queueJob(job);

    }
}
