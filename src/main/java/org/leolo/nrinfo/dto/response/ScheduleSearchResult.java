package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class ScheduleSearchResult {
    private TrainScheduleSummary summary;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TrainScheduleSummary baseSchedule;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<TrainScheduleEntry> details = new ArrayList<TrainScheduleEntry>();
}
