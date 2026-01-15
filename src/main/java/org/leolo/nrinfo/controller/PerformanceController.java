package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.response.PerformanceData;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.service.PermissionService;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController

@RequestMapping("/api/performance")
public class PerformanceController {

    private Logger log = LoggerFactory.getLogger(PerformanceController.class);

    @Autowired
    private RealTimePerformanceService realTimePerformanceService;
    @Autowired
    private PermissionService permissionService;

    @RequestMapping("national")
    public ResponseEntity getNationalPerformance() {
        log.info("getting national performance");
        RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
        if (snapshot == null) {
            log.info("No snapshot available!");

            return ResponseEntity.ok(Map.of(
                    "result", "failed",
                    "message", "No recent performance data received yet"
            ));
        }
        PerformanceData pd = realTimePerformanceService.getNationalPerformanceData(snapshot);
        return ResponseEntity.ok(Map.of(
                "result","success",
                "PPMData", pd,
                "update_time", snapshot.getSnapshotTime(),
                "resp_time", Instant.now()
        ));
    }

}
