package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceDetail {
    @JsonProperty("Total") private int total;
    @JsonProperty("OnTime") private int onTime;
    @JsonProperty("Late") private int late;
    @JsonProperty("CancelVeryLate") private int cancelVeryLate;
    @JsonProperty("PPM") private PerformanceMetric performanceMetric;
    @JsonProperty("RollingPPM") private PerformanceMetric rollingPerformanceMetric;
}
