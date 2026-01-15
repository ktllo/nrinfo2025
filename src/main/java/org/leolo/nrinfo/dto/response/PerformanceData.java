package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class PerformanceData {
    private PerformanceMetric summary;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<PerformanceMetric> sectors = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<PerformanceMetric> operators = new ArrayList<>();
}
