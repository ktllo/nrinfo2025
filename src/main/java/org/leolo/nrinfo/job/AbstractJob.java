package org.leolo.nrinfo.job;

import org.leolo.nrinfo.model.Job;

public abstract class AbstractJob extends Job {

    public abstract void run();

    @Override public Runnable getJob() {
        return AbstractJob.this::run;
    }

    public AbstractJob() {
    }

    public AbstractJob(String jobClass) {
        super(jobClass);
    }
}
