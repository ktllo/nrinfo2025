package org.leolo.nrinfo.enums;

import org.leolo.nrinfo.service.RealTimePerformanceService;

public enum Trend {
    UP,
    STABLE,
    DOWN;

    public static Trend get(String trend) {
        if (trend == null) {
            return null;
        }
        return switch (trend) {
            case "+" -> UP;
            case "=" -> STABLE;
            case "-" -> DOWN;
            default -> null;
        };
    }
}
