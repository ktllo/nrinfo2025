package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.job.NaPTANImportJob;
import org.leolo.nrinfo.model.Job;
import org.leolo.nrinfo.model.JobRecord;
import org.leolo.nrinfo.service.APIAuthenticationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.service.UserPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    @Autowired private ApplicationContext applicationContext;

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

    @RequestMapping({"job/queue/naptan","job/queue/nptg"})
    public ResponseEntity queueNaPTANJob() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("LOAD_NAPTAN")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        Job job = applicationContext.getBean(NaPTANImportJob.class);
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
        org.leolo.nrinfo.dto.response.Job jobDTO = org.leolo.nrinfo.dto.response.Job.toDTO(job);
        jobDTO.setOutput(jobService.getMessages(jobId));
        return ResponseEntity.ok(Map.of("result","success","job",jobDTO));
    }

}
