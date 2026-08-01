package org.leolo.nrinfo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.PerformanceEntry;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.model.ai.CompletionResult;
import org.leolo.nrinfo.model.ai.Prompt;
import org.leolo.nrinfo.model.ai.SystemPrompt;
import org.leolo.nrinfo.model.ai.UserPrompt;
import org.leolo.nrinfo.service.AIGenerationService;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.*;

@RestController
public class AITestController {

    private Logger logger = LoggerFactory.getLogger(AITestController.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired private AIGenerationService aiGenerationService;

    @Autowired
    public RealTimePerformanceService realTimePerformanceService;


    @GetMapping("/ai/performance/summary/{opc}")
    public Object performanceSummary(@PathVariable String opc) {
        //Step 1: Build data
        logger.info("Operator {} performance requested", opc);
        RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
        if (snapshot == null) {
            logger.warn("No real time performance snapshot found");
            return Map.of("message","No data received yet");
        }
        PerformanceEntry pe = snapshot.getOperatorDetails().get(opc);
        if (pe == null) {
            logger.warn("Operator {} not found", opc);
            return Map.of("message","Operator not found");
        }
        PerformanceSummaryData psd = new PerformanceSummaryData();
        psd.snapshotTime = new Date(snapshot.getSnapshotTime().getEpochSecond());
        psd.operatorName = pe.getName();
        psd.threshold = pe.getThreshold();
        psd.sectors = new LinkedList<>();

        for (PerformanceEntry pes :pe.getSubentry()) {
            SectorSummaryData ssd = new SectorSummaryData();
            ssd.sectorName = pes.getName();
            ssd.onTime = pes.getOnTime();
            ssd.late = pes.getLate();
            ssd.cancelled = pes.getCancelOrVeryLate();
            psd.sectors.add(ssd);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(psd);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //Step 2: Build Prompt


        String userPrompt = "```json" + "\n" +
                jsonString + "\n" +
                "```" + "\n";
        List<Prompt> prompts = List.of(
                new SystemPrompt(Constants.AIPrompt.SYSTEM_PERFORMANCE_SUMMARY),
                new UserPrompt(userPrompt)
        );
        CompletionResult result = aiGenerationService.doCompletion(prompts, 5000);
        String resultString = result.getResult()!=null?result.getResult().getFirst() : "Unable to create a brief summary";
        return Map.of("message",resultString);
    }

    @Getter
    class PerformanceSummaryData implements Serializable {
        String operatorName;
        int threshold;
        Collection<SectorSummaryData> sectors;
        Date snapshotTime;
    }

    @Getter
    class SectorSummaryData implements Serializable {
        String sectorName;
        int onTime;
        int late;
        int cancelled;
    }

}
