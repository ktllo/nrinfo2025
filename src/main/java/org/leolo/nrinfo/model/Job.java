package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.util.CommonUtil;

import java.util.Date;
import java.util.UUID;

@Getter
public class Job {

    @Setter private UUID jobId;
    @Setter  int jobOwner;
    @Setter private String jobClass;
    @Setter private Date submittedTime;
    @Setter private Date startTime;
    @Setter private Date finishedTime;

    private Runnable job;

    public Job() {
        jobId = CommonUtil.generateUUID();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        jobClass = stackTraceElements[0].getClassName();
        submittedTime = new Date();
    }
    public Job(Runnable job) {
        jobId = CommonUtil.generateUUID();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        jobClass = stackTraceElements[0].getClassName();
        submittedTime = new Date();
        this.job = job;
    }

    public Job(String jobClass) {
        jobId = CommonUtil.generateUUID();
        this.jobClass = jobClass;
        submittedTime = new Date();

    }
    public Job(String jobClass, Runnable job) {
        jobId = CommonUtil.generateUUID();
        this.jobClass = jobClass;
        submittedTime = new Date();
        this.job = job;
    }

}
