package org.leolo.nrinfo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class JobService {

    @Autowired private ConfigurationService configurationService;

    private ExecutorService executor;

    public JobService() {
        executor = Executors.newFixedThreadPool(
                Integer.parseInt(configurationService.getConfiguration("job.threadpool.size", "3"))
        );
    }
}
