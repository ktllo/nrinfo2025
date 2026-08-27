package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.response.PerformanceData;
import org.leolo.nrinfo.exception.ResourceNotFoundException;
import org.leolo.nrinfo.model.PendingData;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.model.ai.CompletionResult;
import org.leolo.nrinfo.service.AIGenerationService;
import org.leolo.nrinfo.service.GenericCacheService;
import org.leolo.nrinfo.service.PermissionService;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.leolo.nrinfo.util.MarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static org.leolo.nrinfo.Constants.CacheKey.NATIONAL_SUMMARY;
import static org.leolo.nrinfo.Constants.CacheKey.OPERATOR_SUMMARY_TEMPLATE;

@RestController

@RequestMapping("/api/performance")
public class PerformanceController {

    private Logger log = LoggerFactory.getLogger(PerformanceController.class);

    @Autowired
    private RealTimePerformanceService realTimePerformanceService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private AIGenerationService aiGenerationService;
    @Autowired
    private GenericCacheService genericCacheService;

    private final Object LOCK = new Object();



    //Cache

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

    @RequestMapping("summary")
    public ResponseEntity getNationalPerformanceSummary() {
        log.info("getting national performance summary");
        CompletionResult completionResult = null;
        if (genericCacheService.hasEntry(NATIONAL_SUMMARY)) {
            Object obj = genericCacheService.getEntry(NATIONAL_SUMMARY);
            if (obj instanceof CompletionResult) {
                completionResult = (CompletionResult) obj;
            }
        }
        if (completionResult == null) {
            RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
            if (snapshot == null) {
                log.info("No snapshot available!");
                return ResponseEntity.ok(Map.of(
                        "result", "failed",
                        "message", "No recent performance data received yet"
                ));
            }
            completionResult = realTimePerformanceService.getNationalSummary(snapshot);
        }

        String resultString = completionResult.getChoices()!=null?completionResult.getChoices().getFirst() : "Unable to create a brief summary";
        return ResponseEntity.ok(Map.of(
                "message", MarkdownUtil.markdownToHtml(resultString),
                "generated", new SimpleDateFormat("HH:mm:ss").format(new Date(completionResult.getCreatedTime().toEpochMilli())),
                "token_used", completionResult.getTotalTokens(),
                "time_taken", completionResult.getTimeTaken()
        ));
    }

    @RequestMapping("summary/{id}")
    public ResponseEntity getOperatorPerformanceSummary(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return ResponseUtil.buildBadRequestResponse();
        }
        RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
        if (snapshot == null) {
            log.info("No snapshot available!");
            return ResponseEntity.ok(Map.of(
                    "result", "failed",
                    "message", "No recent performance data received yet"
            ));
        }
        final String CACHE_KEY = String.format(OPERATOR_SUMMARY_TEMPLATE, id);
        CompletionResult completionResult = null;
        PendingData<?> pendingData = null;
        synchronized (LOCK) {
            if (!genericCacheService.hasEntry(CACHE_KEY)) {
                pendingData = new PendingData<>();
                genericCacheService.addToCache(CACHE_KEY, pendingData, 600000, GenericCacheService.CacheMode.FIXED_LIFETIME, false);
                try {
                    realTimePerformanceService.getOperatorSummary(snapshot, id, CACHE_KEY);
                } catch (ResourceNotFoundException e) {
                    return ResponseUtil.buildNotFoundResponse();
                }
            } else {
                Object pd = genericCacheService.getEntry(CACHE_KEY);
                if (pd instanceof PendingData) {
                    pendingData = (PendingData<?>) genericCacheService.getEntry(CACHE_KEY);
                } else {
                    throw new RuntimeException("Type mismatch from Generic Cache!");
                }
            }
        }
        Object pedingDataResult = pendingData.getData();
        if (pedingDataResult instanceof CompletionResult) {
            completionResult = (CompletionResult) pendingData.getData();
        } else {
            throw new RuntimeException("Type mismatch from PendingData in Generic Cache!");
        }

        String resultString = completionResult.getChoices()!=null?completionResult.getChoices().getFirst() : "Unable to create a brief summary";
        return ResponseEntity.ok(Map.of(
                "message", MarkdownUtil.markdownToHtml(resultString),
                "generated", new SimpleDateFormat("HH:mm:ss").format(new Date(completionResult.getCreatedTime().toEpochMilli())),
                "token_used", completionResult.getTotalTokens(),
                "time_taken", completionResult.getTimeTaken()
        ));
    }



}
