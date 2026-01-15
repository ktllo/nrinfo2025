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
public class PerformanceMetric {
    @JsonProperty("rag") private String rag;
    private boolean displayRag;
    @JsonProperty("text") private int value;
    @JsonProperty("trendInd") private String trend;

    @JsonProperty("ragDisplayFlag")
    private void setDisplayRag(String displayRag) {
        this.displayRag = "Y".equalsIgnoreCase(displayRag);
    }

}
