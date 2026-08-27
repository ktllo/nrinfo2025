package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainSchedule {
    private String trainUid;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private String trainType;
    private String trainOperator;
    private String scheduleStartDate;
    private String scheduleEndDate;
    private String scheduleDayRuns;
    private String headcode;
    private String retailCode;
    private String powerType;
    private String timingLoad;
    private int plannedSpeed;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> operatingCharacteristic = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TrainScheduleEntry> locations = new ArrayList<>();
}
