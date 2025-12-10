package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Time;

@Getter
@Setter
@ToString
public class ScheduleDetail {
    private String location;
    private int locationInstance;
    private Time arrivalTime;
    private Time departureTime;
    private Time passTime;
    private Time publicArrivalTime;
    private Time publicDepartureTime;

    private String platform;
    private String line;
    private String path;

    private Time engineeringAllowance;
    private Time pathingAllowance;
    private Time performanceAllowance;
}
