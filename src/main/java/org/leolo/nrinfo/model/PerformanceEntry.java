package org.leolo.nrinfo.model;

import lombok.*;
import org.leolo.nrinfo.enums.RAG;
import org.leolo.nrinfo.enums.Trend;
import org.leolo.nrinfo.service.RealTimePerformanceService;

import java.util.ArrayList;
import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerformanceEntry {
    String name;
    String code;
    int total;
    int onTime;
    int late;
    int cancelOrVeryLate;
    int ppmValue;
    RAG ragValue;
    int rollingPpmValue;
    RAG rollingRagValue;
    Trend trend;
    int timeBand;
    Collection<PerformanceEntry> subentry = new ArrayList<>();
}
