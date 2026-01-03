package org.leolo.nrinfo.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceMetric {
    private String name;
    private int onTimeRate;
    private String rag;
}
