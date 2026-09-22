package org.leolo.nrinfo.dto.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleEntry {

    private String stationName;
    private String platform;
    private String crsCode;

    private String gbttArrivalTime;
    private String gbttDepartureTime;

    private String wttArrivalTime;
    private String wttDepartureTime;

    private String engineeringAllowance;
    private String pathingAllowance;
    private String performanceAllowance;

    private String lineOut;
    private String pathIn;

    private boolean passOnly;

}
