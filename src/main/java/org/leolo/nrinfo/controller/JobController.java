package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.request.JobSearch;
import org.leolo.nrinfo.job.*;
import org.leolo.nrinfo.model.Job;
import org.leolo.nrinfo.model.JobRecord;
import org.leolo.nrinfo.service.APIAuthenticationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.service.UserPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

    @RequestMapping("job/queue/schedule/full")
    public ResponseEntity queueFullScheduleImportJob() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("NR_SCHEDULE_LOAD")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        Job job = applicationContext.getBean(NetworkRailScheduleImportJob.class);
        job.setJobOwner(authenticationService.getUserId());
        jobService.queueJob(job);
        return ResponseEntity.ok(Map.of("result","success","jobId",job.getJobId()));
    }

    @RequestMapping("job/queue/schedule/diff")
    public ResponseEntity queueDiffScheduleImportJob() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("NR_SCHEDULE_LOAD")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        NetworkRailScheduleImportJob job = applicationContext.getBean(NetworkRailScheduleImportJob.class);
        job.setJobOwner(authenticationService.getUserId());
        String url = String.format(NetworkRailScheduleImportJob.DIFF_URL, new SimpleDateFormat("EEE").format(new Date()).toLowerCase());
        log.info("Job url: {}", url);
        job.setUrl(url);
        jobService.queueJob(job);
        return ResponseEntity.ok(Map.of("result","success","jobId",job.getJobId()));
    }

    @RequestMapping("job/queue/schedule/build_cache")
    public ResponseEntity queueScheduleCacheJob() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("NR_SCHEDULE_LOAD")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        ScheduleCacheJob job = applicationContext.getBean(ScheduleCacheJob.class);
        job.setJobOwner(authenticationService.getUserId());
        jobService.queueJob(job);
        return ResponseEntity.ok(Map.of("result","success","jobId",job.getJobId()));
    }

    @RequestMapping("job/queue/schedule/prune")
    public ResponseEntity queueSchedulePruneJob() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("NR_SCHEDULE_LOAD")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        AbstractJob job = applicationContext.getBean(SchedulePruneJob.class);
        job.setJobOwner(authenticationService.getUserId());
        jobService.queueJob(job);
        return ResponseEntity.ok(Map.of("result","success","jobId",job.getJobId()));
    }

    @RequestMapping({"job/queue/nrrefdata",})
    public ResponseEntity queueNetworkRailReferenceData() {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("NR_REF_DATA_LOAD")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        Job job = applicationContext.getBean(NetworkRailReferenceDataJob.class);
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

    @RequestMapping("/job/search")
    public ResponseEntity searchJob(@RequestBody JobSearch jobSearch) {
        if (!authenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("VIEW_ALL_JOBS")) {
            if (jobSearch.getUsername() == null || jobSearch.getUsername().isEmpty()) {
                // Add in the username
                jobSearch.setUsername(authenticationService.getUsername());
            } else if (!jobSearch.getUsername().equals(authenticationService.getUsername())) {
                return ResponseUtil.buildForbiddenResponse();
            }
        } else {
            log.info("User can see all jobs");
        }
        log.info("Job search started - {}", jobSearch);
        jobSearch.validate();
        Collection<JobRecord> jobs = jobService.searchJobs(jobSearch);
        ArrayList<org.leolo.nrinfo.dto.response.Job> jobList = new ArrayList<org.leolo.nrinfo.dto.response.Job>();
        for (JobRecord job : jobs) {
            org.leolo.nrinfo.dto.response.Job dto = org.leolo.nrinfo.dto.response.Job.toDTO(job);
            dto.setOutput(jobService.getMessages(dto.getJobId()));
            jobList.add(dto);
        }
        return ResponseEntity.ok(Map.of("result","success","size",jobList.size(),"jobs",jobList));
    }

}
