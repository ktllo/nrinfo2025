package org.leolo.nrinfo.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

@Getter
@Setter
public class Job {
    private String jobId;
    private String jobClass;
    private Date submittedTime;
    private Date startTime;
    private Date endTime;
    private String status;
    private Collection<JobMessage> output;
    public static Job toDTO(org.leolo.nrinfo.model.JobRecord job) {
        Job jobDTO = new Job();
        jobDTO.jobId = job.getJobId().toString();
        jobDTO.jobClass = job.getJobClass();
        jobDTO.submittedTime = job.getSubmittedTime();
        jobDTO.startTime = job.getStartTime();
        jobDTO.endTime = job.getFinishedTime();
        if ("Q".equals(job.getStatus())) {
            jobDTO.status = "Queued";
        } else if ("D".equals(job.getStatus())) {
            jobDTO.status = "Done";
        } else if ("R".equals(job.getStatus())) {
            jobDTO.status = "Running";
        } else if ("F".equals(job.getStatus())) {
            jobDTO.status = "Failed";
        } else {
            jobDTO.status = "Unknown";
        }
        return jobDTO;
    }

    public String getWaitTime() {
        if (submittedTime != null && startTime != null) {
            return getDisplayedDuration(submittedTime.getTime(), startTime.getTime());
        }
        return null;
    }

    public String getRunningTime() {
        if (startTime != null && endTime != null) {
            return getDisplayedDuration(startTime.getTime(), endTime.getTime());
        }
        return null;
    }

    private String getDisplayedDuration(long start, long end) {
        Duration duration = Duration.of(end - start, ChronoUnit.MILLIS);
        return String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
