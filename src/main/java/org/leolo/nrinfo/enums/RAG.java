package org.leolo.nrinfo.enums;

import org.leolo.nrinfo.service.RealTimePerformanceService;

public enum RAG {
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
