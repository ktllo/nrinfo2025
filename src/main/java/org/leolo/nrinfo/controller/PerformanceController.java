package org.leolo.nrinfo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dto.response.PerformanceData;
import org.leolo.nrinfo.model.PerformanceEntry;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.model.ai.CompletionResult;
import org.leolo.nrinfo.model.ai.Prompt;
import org.leolo.nrinfo.model.ai.SystemPrompt;
import org.leolo.nrinfo.model.ai.UserPrompt;
import org.leolo.nrinfo.service.AIGenerationService;
import org.leolo.nrinfo.service.GenericCacheService;
import org.leolo.nrinfo.service.PermissionService;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

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

    private ObjectMapper objectMapper = new ObjectMapper();

    public static final String NATIONAL_SUMMARY = "perfsummary.national";

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
            //We need to generate the result
            log.info("Cache missed, going to generate summary");
            RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
            if (snapshot == null) {
                log.info("No snapshot available!");
                return ResponseEntity.ok(Map.of(
                        "result", "failed",
                        "message", "No recent performance data received yet"
                ));
            }
            PerformanceSummaryData psd = new PerformanceSummaryData();
            psd.operatorName = "National Rail"; //This is a generic placeholder
            psd.snapshotTime = new Date(snapshot.getSnapshotTime().toEpochMilli());
            psd.threshold = 0;
            for (PerformanceEntry pe : snapshot.getNationalSector()) {
                SectorSummaryData ssd = new SectorSummaryData();
                ssd.sectorName = pe.getName();
                ssd.onTime = pe.getOnTime();
                ssd.late = pe.getLate();
                ssd.cancelled = pe.getCancelOrVeryLate();
                psd.sectors.add(ssd);
            }
            String userPrompt = null;
            try {
                userPrompt = "```json" + "\n" +
                        objectMapper.writeValueAsString(psd) + "\n" +
                        "```" + "\n";
            } catch (JsonProcessingException e) {
                log.error("Unable to serialize psd", e);
                return ResponseUtil.buildFullErrorResponse("Serialization error", "Unable to serialize performance data");
            }
            List<Prompt> prompts = List.of(
                    new SystemPrompt(Constants.AIPrompt.SYSTEM_PERFORMANCE_SUMMARY),
                    new UserPrompt(userPrompt)
            );
            completionResult = aiGenerationService.doCompletion(prompts, 5000);
            genericCacheService.addToCache(NATIONAL_SUMMARY, completionResult, 60000, GenericCacheService.CacheMode.FIXED_LIFETIME, false);
        }

        String resultString = completionResult.getChoices()!=null?completionResult.getChoices().getFirst() : "Unable to create a brief summary";
        return ResponseEntity.ok(Map.of("message",resultString));
    }

    @Getter
    static
    class PerformanceSummaryData implements Serializable {
        String operatorName;
        int threshold;
        Collection<SectorSummaryData> sectors = new ArrayList<>();
        Date snapshotTime;
    }

    @Getter
    static
    class SectorSummaryData implements Serializable {
        String sectorName;
        int onTime;
        int late;
        int cancelled;
    }

}
