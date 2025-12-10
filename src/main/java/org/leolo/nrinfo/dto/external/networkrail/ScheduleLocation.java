package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Time;

@Getter
@Setter
@ToString
public class ScheduleLocation {
    @JsonProperty("record_identity") private String recordIdentity;
    @JsonProperty("tiploc_code") private String tiplocCode;
    @JsonProperty(value = "tiploc_instance", defaultValue = "0") private int tiplocInstance;

    //Working Timetable
    @JsonProperty("arrival") private String arrivalTime;
    @JsonProperty("departure") private String departureTime;
    @JsonProperty("pass") private String passTime;

    //Public Timetable
    @JsonProperty("public_arrival") private String publicArrivalTime;
    @JsonProperty("public_departure") private String publicDepartureTime;

    @JsonProperty("platform") private String platform;
    //Departure Line, Arrival Path
    @JsonProperty("line") private String line;
    @JsonProperty("path") private String path;

    @JsonProperty("engineering_allowance") private String engineeringAllowance;
    @JsonProperty("pathing_allowance") private String pathingAllowance;
    @JsonProperty("performance_allowance") private String performanceAllowance;

    @JsonProperty("location_type") private String locationType;
}
