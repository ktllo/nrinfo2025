package org.leolo.nrinfo.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.leolo.nrinfo.dto.external.networkrail.PerformanceDetail;
import org.leolo.nrinfo.dto.external.networkrail.RealTimePerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class RealTimePerformanceService {

    private Logger log = LoggerFactory.getLogger(RealTimePerformanceService.class);

    private static final Object SYNC_LOCK = new Object();

    private RealTimePerformanceSnapshot snapshot;

    public RealTimePerformanceSnapshot getSnapshot() {
        synchronized (SYNC_LOCK) {
            return snapshot;
        }
    }


    public void submitNewSnapshot(RealTimePerformance rtp) {
        if (rtp == null) {
            log.info("Null snapshot given, ignoring");
            return;
        }
        RealTimePerformanceSnapshot snapshot = new RealTimePerformanceSnapshot();
        snapshot.snapshotTime = Instant.ofEpochMilli(rtp.getTimestamp());
        PerformanceDetail nationalPerf = rtp.getRtppmData().getNationalPage().getNationalPerformance();
        snapshot.nationalPerformance = PerformanceEntry.builder()
                .name("National Performance")
                .ppmValue(nationalPerf.getPerformanceMetric().getValue())
                .ragValue(RAG.get(nationalPerf.getPerformanceMetric().getRag()))
                .rollingPpmValue(nationalPerf.getRollingPerformanceMetric().getValue())
                .rollingRagValue(RAG.get(nationalPerf.getRollingPerformanceMetric().getRag()))
                .trend(Trend.get(nationalPerf.getRollingPerformanceMetric().getTrend()))
                .build();
        synchronized (SYNC_LOCK) {
            if (this.snapshot == null || this.snapshot.snapshotTime.isBefore(snapshot.snapshotTime)) {
                this.snapshot = snapshot;
                log.info("Replaced snapshot, new snapshot time is {}, which is {}s old",
                        snapshot.snapshotTime,
                        Duration.between(snapshot.snapshotTime, Instant.now()).toSeconds()
                );
            } else {
                log.warn("Snapshot not replaced because offered one is older");
            }
        }
    }

    @Getter
    public static class RealTimePerformanceSnapshot {
        Instant snapshotTime;
        PerformanceEntry nationalPerformance;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PerformanceEntry {
        String name;
        int total;
        int onTime;
        int late;
        int cancelOrVeryLate;
        int ppmValue;
        RAG ragValue;
        int rollingPpmValue;
        RAG rollingRagValue;
        Trend trend;

    }

    public static enum RAG {
        GREEN,
        AMBER,
        RED;

        public static RAG get(String rag) {
            return switch (rag) {
                case "G" -> GREEN;
                case "A" -> AMBER;
                case "R" -> RED;
                default -> null;
            };
        }
    }

    public static enum Trend {
        UP,
        STABLE,
        DOWN;

        public static Trend get(String trend) {
            return switch (trend) {
                case "+" -> UP;
                case "=" -> STABLE;
                case "-" -> DOWN;
                default -> null;
            };
        }
    }
}
