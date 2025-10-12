package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
public class Job {

    @Setter private UUID jobId;
    @Setter  int jobOwner;
    @Setter private Class jobClass;
    @Setter private Date submittedTime;
    @Setter private Date startTime;
    @Setter private Date finishedTime;

    private Runnable job;

    public Job() {
//        jobId = CommonU
    }

}
