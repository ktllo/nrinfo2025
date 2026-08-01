package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VstpScheduleSegment {
    @JsonProperty("schedule_location") private ArrayList<VstpScheduleLocation> scheduleLocation;
    @JsonProperty("signalling_id") private String signallingId;
    @JsonProperty("atoc_code") private String atocCode;
    @JsonProperty("CIF_train_service_code") private String trainServiceCode;
    @JsonProperty("CIF_train_category") private String trainCategory;
    @JsonProperty("CIF_timing_load") private String timingLoad;
    @JsonProperty("CIF_speed") private String speed;
    @JsonProperty("CIF_power_type") private String powerType;
    @JsonProperty("CIF_course_indicator") private String courseIndicator;
}
