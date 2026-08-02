package org.leolo.nrinfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.controller.AITestController;
import org.leolo.nrinfo.controller.PerformanceController;
import org.leolo.nrinfo.controller.ResponseUtil;
import org.leolo.nrinfo.dto.external.networkrail.PerformanceDetail;
import org.leolo.nrinfo.dto.external.networkrail.RealTimePerformance;
import org.leolo.nrinfo.dto.external.networkrail.RealTimePerformanceData;
import org.leolo.nrinfo.dto.response.PerformanceData;
import org.leolo.nrinfo.dto.response.PerformanceMetric;
import org.leolo.nrinfo.enums.RAG;
import org.leolo.nrinfo.enums.Trend;
import org.leolo.nrinfo.exception.ResourceNotFoundException;
import org.leolo.nrinfo.model.PerformanceEntry;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.model.ai.CompletionResult;
import org.leolo.nrinfo.model.ai.Prompt;
import org.leolo.nrinfo.model.ai.SystemPrompt;
import org.leolo.nrinfo.model.ai.UserPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.leolo.nrinfo.Constants.CacheKey.NATIONAL_SUMMARY;

@Service
public class RealTimePerformanceService {

    private Logger log = LoggerFactory.getLogger(RealTimePerformanceService.class);

    private static final Object SYNC_LOCK = new Object();

    @Autowired
    private AIGenerationService aiGenerationService;

    @Autowired
    private GenericCacheService genericCacheService;

    private RealTimePerformanceSnapshot snapshot;

    public RealTimePerformanceSnapshot getSnapshot() {
        synchronized (SYNC_LOCK) {
            return snapshot;
        }
    }


    private ObjectMapper objectMapper = new ObjectMapper();


    public void submitNewSnapshot(RealTimePerformance rtp) {
        if (rtp == null) {
            log.info("Null snapshot given, ignoring");
            return;
        }
        RealTimePerformanceSnapshot snapshot = new RealTimePerformanceSnapshot();
        snapshot.setSnapshotTime(Instant.ofEpochMilli(rtp.getTimestamp()));
        PerformanceDetail nationalPerf = rtp.getRtppmData().getNationalPage().getNationalPerformance();
        snapshot.setNationalPerformance(PerformanceEntry.builder()
                .name("National Performance")
                .ppmValue(nationalPerf.getPerformanceMetric().getValue())
                .ragValue(RAG.get(nationalPerf.getPerformanceMetric().getRag()))
                .rollingPpmValue(nationalPerf.getRollingPerformanceMetric().getValue())
                .rollingRagValue(RAG.get(nationalPerf.getRollingPerformanceMetric().getRag()))
                .trend(Trend.get(nationalPerf.getRollingPerformanceMetric().getTrend()))
                .build());
        for(RealTimePerformanceData.SectorPerformance sectorPerformance: rtp.getRtppmData().getNationalPage().getSectorPerformance()) {
            snapshot.getNationalSector().add(
                    PerformanceEntry.builder()
                            .name(sectorPerformance.getSectorDesc())
                            .code(sectorPerformance.getSectorCode())
                            .ppmValue(sectorPerformance.getSectorPerformance().getPerformanceMetric().getValue())
                            .ragValue(RAG.get(sectorPerformance.getSectorPerformance().getPerformanceMetric().getRag()))
                            .rollingPpmValue(sectorPerformance.getSectorPerformance().getRollingPerformanceMetric().getValue())
                            .rollingRagValue(RAG.get(sectorPerformance.getSectorPerformance().getRollingPerformanceMetric().getRag()))
                            .total(sectorPerformance.getSectorPerformance().getTotal())
                            .onTime(sectorPerformance.getSectorPerformance().getOnTime())
                            .late(sectorPerformance.getSectorPerformance().getLate())
                            .cancelOrVeryLate(sectorPerformance.getSectorPerformance().getCancelVeryLate())
                            .trend(Trend.get(sectorPerformance.getSectorPerformance().getRollingPerformanceMetric().getTrend()))
                            .build()
            );
        }
        for(RealTimePerformanceData.OperatorPerformance operatorPerformance: rtp.getRtppmData().getNationalPage().getOperatorPerformance()) {
            PerformanceEntry pe = parseData(operatorPerformance);
            snapshot.getNationalOperator().put(pe.getCode(), pe);
        }
        for (RealTimePerformanceData.OperatorPerformance operatorPerformance: rtp.getRtppmData().getOocPage().getOperatorPerformance()) {
            PerformanceEntry pe = parseData(operatorPerformance);
            snapshot.getNationalOperator().put(pe.getCode(), pe);
        }
        for (RealTimePerformanceData.OperatorPerformance operatorPerformance: rtp.getRtppmData().getFocPage().getOperatorPerformance()) {
            PerformanceEntry pe = parseData(operatorPerformance);
            snapshot.getNationalOperator().put(pe.getCode(), pe);
        }
        for (RealTimePerformanceData.OperatorPage op : rtp.getRtppmData().getOperatorPages()) {
            int threshold = 0;
            if(op.getPerformanceTolerance() != null) {
                for (RealTimePerformanceData.PerformanceTolerance pt : op.getPerformanceTolerance()) {
                    threshold = pt.getTimeband();
                    break;
                }
            }
            PerformanceEntry pe = PerformanceEntry.builder()
                    .code(op.getOperatorPerformanceDetail().getCode())
                    .name(op.getOperatorPerformanceDetail().getName())
                    .total(op.getOperatorPerformanceDetail().getTotal())
                    .onTime(op.getOperatorPerformanceDetail().getOnTime())
                    .late(op.getOperatorPerformanceDetail().getLate())
                    .cancelOrVeryLate(op.getOperatorPerformanceDetail().getCancelVeryLate())
                    .ppmValue(op.getOperatorPerformanceDetail().getPerformanceMetric().getValue())
                    .ragValue(RAG.get(op.getOperatorPerformanceDetail().getPerformanceMetric().getRag()))
                    .rollingPpmValue(op.getOperatorPerformanceDetail().getRollingPerformanceMetric().getValue())
                    .rollingRagValue(RAG.get(op.getOperatorPerformanceDetail().getRollingPerformanceMetric().getRag()))
                    .trend(Trend.get(op.getOperatorPerformanceDetail().getRollingPerformanceMetric().getTrend()))
                    .threshold(threshold)
                    .build();
            if (op.getOperatorServiceGroup()!=null) {
                pe.setSubentry(new ArrayList<>());
                for (RealTimePerformanceData.OperatorServiceGroup osg : op.getOperatorServiceGroup()) {
                    PerformanceEntry spe = PerformanceEntry.builder()
                            .code(osg.getSectorCode())
                            .name(osg.getName())
                            .ppmValue(osg.getPerformanceMetric().getValue())
                            .ragValue(RAG.get(osg.getPerformanceMetric().getRag()))
                            .rollingPpmValue(osg.getRollingPerformanceMetric().getValue())
                            .rollingRagValue(RAG.get(osg.getRollingPerformanceMetric().getRag()))
                            .trend(Trend.get(osg.getRollingPerformanceMetric().getTrend()))
                            .total(osg.getTotal())
                            .onTime(osg.getOnTime())
                            .late(osg.getLate())
                            .cancelOrVeryLate(osg.getCancelVeryLate())
                            .timeBand(osg.getTimeband())
                            .build();
                    pe.getSubentry().add(spe);
                }
            }
            snapshot.getOperatorDetails().put(pe.getCode(), pe);
        }
        synchronized (SYNC_LOCK) {
            if (this.snapshot == null || this.snapshot.getSnapshotTime().isBefore(snapshot.getSnapshotTime())) {
                this.snapshot = snapshot;
                log.info("Replaced snapshot, new snapshot time is {}, which is {}s old",
                        snapshot.getSnapshotTime(),
                        Duration.between(snapshot.getSnapshotTime(), Instant.now()).toSeconds()
                );
            } else {
                log.warn("Snapshot not replaced because offered one is older");
            }
        }
    }

    public PerformanceData getOperatorData(PerformanceEntry pe) {
        PerformanceData pd = new PerformanceData();
        PerformanceMetric pm = new PerformanceMetric();
        pm.setCode(pe.getCode());
        pm.setName(pe.getName());
        pm.setTotal(pe.getTotal());
        pm.setOnTime(pe.getOnTime());
        pm.setLate(pe.getLate());
        pm.setCancelOrVeryLate(pe.getCancelOrVeryLate());
        pm.setOnTimeRate(pe.getPpmValue());
        pm.setRag(pe.getRagValue());
        pm.setRollingOnTimeRate(pe.getRollingPpmValue());
        pm.setRollingRag(pe.getRollingRagValue());
        pm.setTrend(pe.getTrend());
        pd.setSummary(pm);
        if(pe.getSubentry() != null) {
            for (PerformanceEntry spe : pe.getSubentry()) {
                pm = new PerformanceMetric();
                pm.setCode(spe.getCode());
                pm.setName(spe.getName());
                pm.setTotal(spe.getTotal());
                pm.setOnTime(spe.getOnTime());
                pm.setLate(spe.getLate());
                pm.setCancelOrVeryLate(spe.getCancelOrVeryLate());
                pm.setOnTimeRate(spe.getPpmValue());
                pm.setRag(spe.getRagValue());
                pm.setRollingOnTimeRate(spe.getRollingPpmValue());
                pm.setRollingRag(spe.getRollingRagValue());
                pm.setTrend(spe.getTrend());
                pd.getSectors().add(pm);
            }
        }
        return pd;
    }

    private PerformanceEntry parseData(RealTimePerformanceData.OperatorPerformance operatorPerformance) {
        return PerformanceEntry.builder()
                .name(operatorPerformance.getName())
                .code(operatorPerformance.getCode())
                .ppmValue(operatorPerformance.getPerformanceMetric().getValue())
                .ragValue(RAG.get(operatorPerformance.getPerformanceMetric().getRag()))
                .rollingPpmValue(operatorPerformance.getRollingPerformanceMetric().getValue())
                .rollingRagValue(RAG.get(operatorPerformance.getRollingPerformanceMetric().getRag()))
                .total(operatorPerformance.getTotal())
                .trend(Trend.get(operatorPerformance.getRollingPerformanceMetric().getTrend()))
                .build();
    }

    public PerformanceData getNationalPerformanceData(RealTimePerformanceSnapshot snapshot) {
        PerformanceData pd = new PerformanceData();
        PerformanceMetric pm = new PerformanceMetric();
        pm.setName("National");
        pm.setOnTimeRate(snapshot.getNationalPerformance().getPpmValue());
        pm.setRag(snapshot.getNationalPerformance().getRagValue());
        pm.setRollingOnTimeRate(snapshot.getNationalPerformance().getRollingPpmValue());
        pm.setRollingRag(snapshot.getNationalPerformance().getRollingRagValue());
        pm.setTrend(snapshot.getNationalPerformance().getTrend());

        pd.setSummary(pm);
        for (PerformanceEntry pe:snapshot.getNationalSector()) {
            pm = new PerformanceMetric();
            pm.setName(pe.getName());
            pm.setCode(pe.getCode());
            pm.setOnTimeRate(pe.getPpmValue());
            pm.setRag(pe.getRagValue());
            pm.setRollingRag(pe.getRollingRagValue());
            pm.setRollingOnTimeRate(pe.getRollingPpmValue());
            pm.setTotal(pe.getTotal());
            pm.setOnTime(pe.getOnTime());
            pm.setLate(pe.getLate());
            pm.setCancelOrVeryLate(pe.getCancelOrVeryLate());
            pm.setTrend(pe.getTrend());
            pd.getSectors().add(pm);
        }
        for (String code:snapshot.getNationalOperator().keySet()) {
            pm = new PerformanceMetric();
            PerformanceEntry pe = snapshot.getNationalOperator().get(code);
            pm.setName(pe.getName());
            pm.setCode(pe.getCode());
            pm.setOnTimeRate(pe.getPpmValue());
            pm.setTotal(pe.getTotal());
            pd.getOperators().add(pm);
        }
        return pd;
    }

    public CompletionResult getNationalSummary(RealTimePerformanceSnapshot snapshot) {
        if (snapshot == null) {
            log.info("No snapshot available!");
            throw new RuntimeException("No data received yet");
        }
        PerformanceSummaryData psd = new PerformanceSummaryData();
        psd.operatorName = "National Rail"; //This is a generic placeholder
        psd.snapshotTime = new Date(snapshot.getSnapshotTime().toEpochMilli());
        psd.threshold = 0;
        for (PerformanceEntry pe : snapshot.getNationalSector()) {
            psd.sectors.add(extractSectorSummary(pe));
        }
        String userPrompt = null;
        try {
            userPrompt = "```json" + "\n" +
                    objectMapper.writeValueAsString(psd) + "\n" +
                    "```" + "\n";
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize psd", e);
            throw new RuntimeException("Unable to serialize data");
        }
        List<Prompt> prompts = List.of(
                new SystemPrompt(Constants.AIPrompt.SYSTEM_PERFORMANCE_SUMMARY),
                new UserPrompt(userPrompt)
        );
        CompletionResult completionResult = aiGenerationService.doCompletion(prompts, 5000);
        genericCacheService.addToCache(NATIONAL_SUMMARY, completionResult, 600000, GenericCacheService.CacheMode.FIXED_LIFETIME, false);
        return completionResult;
    }

    public CompletionResult getOperatorSummary(RealTimePerformanceSnapshot snapshot, String operatorId, final String CACHE_KEY) {
        if (snapshot == null) {
            log.info("No snapshot available!");
            throw new RuntimeException("No data received yet");
        }
        PerformanceEntry pe = snapshot.getOperatorDetails().get(operatorId);
        PerformanceSummaryData psd = new PerformanceSummaryData();
        if (pe == null) {
            log.warn("Operator {} not found", operatorId);
            throw new ResourceNotFoundException("Operator not found");
        }
        psd.operatorName = pe.getName();
        psd.snapshotTime = new Date(snapshot.getSnapshotTime().toEpochMilli());
        psd.threshold = pe.getThreshold();
        if (pe.getSubentry()!= null && !pe.getSubentry().isEmpty()) {
            for (PerformanceEntry pes :pe.getSubentry()) {
                psd.sectors.add(extractSectorSummary(pes));
            }
        } else {
            SectorSummaryData ssd = new SectorSummaryData();
            ssd.sectorName = "Overall";
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
            throw new RuntimeException("Unable to serialize data");
        }
        List<Prompt> prompts = List.of(
                new SystemPrompt(Constants.AIPrompt.SYSTEM_PERFORMANCE_SUMMARY),
                new UserPrompt(userPrompt)
        );
        CompletionResult completionResult = aiGenerationService.doCompletion(prompts, 5000);
        genericCacheService.addToCache(CACHE_KEY, completionResult, 600000, GenericCacheService.CacheMode.FIXED_LIFETIME, false);
        return completionResult;
    }

    private static @NotNull SectorSummaryData extractSectorSummary(PerformanceEntry pes) {
        SectorSummaryData ssd = new SectorSummaryData();
        ssd.sectorName = pes.getName();
        ssd.onTime = pes.getOnTime();
        ssd.late = pes.getLate();
        ssd.cancelled = pes.getCancelOrVeryLate();
        return ssd;
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
