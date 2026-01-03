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
public class PerformanceRAGThreshold {
    @JsonProperty("type") String type;
    @JsonProperty("good") int goodThreshold;
    @JsonProperty("medium") int mediumThreshold;
}
