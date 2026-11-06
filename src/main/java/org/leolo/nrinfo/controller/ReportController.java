package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.service.CurrentUserService;
import org.leolo.nrinfo.service.ReportService;
import org.leolo.nrinfo.service.WebAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private WebAuthenticationService webAuthenticationService;



    @GetMapping("/status/{requestId}")
    public ResponseEntity statusReport(@PathVariable("requestId") String requestId) {
        UUID uuid = UUID.fromString(requestId);
        ReportService.ReportGenerationStatus status = reportService.getReportGenerationStatus(uuid);
        if (status == null) {
            log.info("Request id {} not found", requestId);
            return ResponseUtil.buildNotFoundResponse();
        }
        boolean ready = status == ReportService.ReportGenerationStatus.FINISHED || status == ReportService.ReportGenerationStatus.RETRIEVED;
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "ready", ready,
                        "generationStatus", status
                )
        );
    }

}
