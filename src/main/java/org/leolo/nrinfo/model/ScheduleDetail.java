package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;

@Getter
@Setter
@ToString
public class ScheduleDetail {
    private String location;
    private int locationInstance;
    private Duration arrivalTime;
    private Duration departureTime;
    private Duration passTime;
    private Duration publicArrivalTime;
    private Duration publicDepartureTime;

    private String platform;
    private String line;
    private String path;

    private Duration engineeringAllowance;
    private Duration pathingAllowance;
    private Duration performanceAllowance;
}
