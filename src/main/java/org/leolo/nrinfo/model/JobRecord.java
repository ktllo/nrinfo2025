package org.leolo.nrinfo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
public class JobRecord {


    private UUID jobId;
    @JsonIgnore
    int jobOwner;
    private String jobClass;
    private Date submittedTime;
    private Date startTime;
    private Date finishedTime;
    private String status;
}
