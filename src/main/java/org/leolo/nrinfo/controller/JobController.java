package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.model.Job;
import org.leolo.nrinfo.model.JobRecord;
import org.leolo.nrinfo.service.APIAuthenticationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.service.UserPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobController {

    private Logger log = LoggerFactory.getLogger(JobController.class);

    @Autowired private JobService jobService;
    @Autowired private APIAuthenticationService authenticationService;
    @Autowired private UserPermissionService userPermissionService;

    @RequestMapping("job/queue/example")
    public ResponseEntity startExampleJob(@RequestParam(name = "sleep", required = false, defaultValue = "60") final int sleepTime) {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("MISC_TEST")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        Job job = new Job(
                new Runnable() {
                    private Logger log = LoggerFactory.getLogger("EXAMPLE-JOB");
                    @Override
                    public void run() {
                        log.info("Job started");
                        try {
                            Thread.sleep(sleepTime* 1000L);
                        } catch (InterruptedException e) {
                            log.error("Sleep interrupted - {}" , e.getMessage());
                        }
                        log.info("Job done");
                    }
                }
        );
        job.setJobOwner(authenticationService.getUserId());
        jobService.queueJob(job);
        return ResponseEntity.ok(Map.of("result","success","jobId",job.getJobId()));
    }

    @RequestMapping("/job/view/{jobid}")
    public ResponseEntity viewJob(@PathVariable("jobid") final String jobId) {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        boolean canViewJob = userPermissionService.hasPermission("VIEW_ALL_JOBS");
        JobRecord job = jobService.getJobRecord(jobId);
        //If user have right to view any job, and job does not exist, return not found
        if (canViewJob && job == null) {
            return ResponseUtil.buildNotFoundResponse();
        }
        //If user does not have right to view any job, and job belongs to others, or does not exist, return forbidden
        if (!canViewJob && (job == null || authenticationService.getUserId() != job.getJobOwner())) {
            return ResponseUtil.buildForbiddenResponse();
        }
        return ResponseEntity.ok(Map.of("result","success","job",job));
    }

}
