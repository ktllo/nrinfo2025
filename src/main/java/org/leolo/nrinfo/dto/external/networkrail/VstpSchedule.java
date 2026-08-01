package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.leolo.nrinfo.util.CommonUtil;

import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VstpSchedule {

    @JsonProperty("schedule_segment")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private ArrayList<VstpScheduleSegment> scheduleSegment;

    @JsonProperty("transaction_type") private String transactionType;
    @JsonProperty("train_status") private String trainStatus;
    @JsonProperty("schedule_start_date") private Date scheduleStartDate;
    @JsonProperty("schedule_end_date") private Date scheduleEndDate;
    @JsonProperty("schedule_days_runs") private String scheduleDaysRuns;
    @JsonProperty("CIF_train_uid") private String trainUid;
    @JsonProperty("CIF_stp_indicator") private String stpIndicator;

    public void setTrainUid(String trainUid) {
        this.trainUid = trainUid.strip();
    }

    public org.leolo.nrinfo.model.Schedule toModel() {
        org.leolo.nrinfo.model.Schedule schedule = new org.leolo.nrinfo.model.Schedule();
        schedule.setScheduleUuid(CommonUtil.generateUUID());
        schedule.setTrainUid(trainUid);
        schedule.setStartDate(scheduleStartDate);
        schedule.setEndDate(scheduleEndDate);
        schedule.setDaysRun(scheduleDaysRuns);
        return schedule;
    }
}
