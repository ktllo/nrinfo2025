package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

@Getter
@Setter
public class RealTimePerformanceSnapshot {
    Instant snapshotTime;
    PerformanceEntry nationalPerformance;
    Collection<PerformanceEntry> nationalSector = new ArrayList<>();
    TreeMap<String, PerformanceEntry> nationalOperator = new TreeMap<>();
    TreeMap<String, PerformanceEntry> operatorDetails = new TreeMap<>();
}
