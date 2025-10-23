package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.JobDao;
import org.leolo.nrinfo.dto.response.JobMessage;
import org.leolo.nrinfo.model.Job;
import org.leolo.nrinfo.model.JobRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class JobService {

    private Logger log = LoggerFactory.getLogger(JobService.class);
    @Autowired private ConfigurationService configurationService;
    @Autowired private JobDao jobDao;

    private ExecutorService executor = null;
    private boolean initialized = false;
    private synchronized void init () {
        if (initialized) {
            return;
        }
        executor = Executors.newFixedThreadPool(
                Integer.parseInt(configurationService.getConfiguration("job.threadpool.size","5"))
        );
        initialized = true;
    }

    public void writeMessage(Job job, String message) {
        try {
            jobDao.insertMessage(job, message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void queueJob(Job job) {
        init(); //Make sure it is ready
        try {
            jobDao.insertJob(job);
        } catch (Exception e) {
            log.error(e.getMessage());
            // We can't queue it!
            return;
        }
        executor.submit(() -> {
            //Mark Start
            try {
                jobDao.markJobStart(job);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            //Run it!
            try {
                job.getJob().run();
            } catch (Throwable t) {
                try {
                    jobDao.markJobFailed(job, t);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                return;
            }
            //Mark Done
            try {
                jobDao.markJobDone(job);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    public JobRecord getJobRecord(String jobId) {
        init();
        try {
            return jobDao.getJobRecord(jobId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<JobMessage> getMessages(String jobId) {
        init();
        try {
            return jobDao.getJobMessages(jobId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
