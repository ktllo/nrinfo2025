package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VstpScheduleLocation {
    private String tiplocId;
    @JsonProperty("scheduled_pass_time") private String scheduledPassTime;
    @JsonProperty("scheduled_departure_time") private String scheduledDepartureTime;
    @JsonProperty("scheduled_arrival_time") private String scheduledArrivalTime;
    @JsonProperty("public_arrival_time") private String publicArrivalTime;
    @JsonProperty("public_departure_time") private String publicDepartureTime;

    @JsonProperty("CIF_platform") private String platform;
    @JsonProperty("CIF_performance_allowance") private String performanceAllowance;
    @JsonProperty("CIF_pathing_allowance") private String pathingAllowance;
    @JsonProperty("CIF_line") private String line;
    @JsonProperty("CIF_path") private String path;
    @JsonProperty("CIF_engineering_allowance") private String engineeringAllowance;
    @JsonProperty("CIF_activity") private String activity;



    @JsonProperty("location")
    public void unpackLocation(JsonNode node) {
        this.tiplocId = node.path("tiploc")
                .path("tiploc_id")
                .asText(null);
    }
}
