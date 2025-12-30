package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.util.ScheduleUtil;

import java.util.Date;

@Setter
@Getter
@ToString
public class Schedule {

    @JsonProperty("CIF_train_uid") private String trainUid;
    @JsonProperty("transaction_type") private String transactionType;
    @JsonProperty("schedule_start_date") private Date scheduleStartDate;
    @JsonProperty("schedule_end_date") private Date scheduleEndDate;
    @JsonProperty("schedule_days_runs") private String scheduleDayRuns;
    //For display only
    @JsonProperty("CIF_bank_holiday_running") private String bankHolidayRunning;

    @JsonProperty("train_status") private String trainStatus;
    @JsonProperty("CIF_stp_indicator") private String stpIndicator;
    @JsonProperty("atoc_code") private String atocCode;
    @JsonProperty("applicable_timetable") private String applicableTimetable;
    @JsonProperty("schedule_segment") private ScheduleSegment scheduleSegment;
    @JsonProperty("new_schedule_segment") private NewScheduleSegment newScheduleSegment;

    public org.leolo.nrinfo.model.Schedule toModel() {
        org.leolo.nrinfo.model.Schedule schedule = new org.leolo.nrinfo.model.Schedule();
        schedule.setScheduleUuid(CommonUtil.generateUUID());
        schedule.setTrainUid(trainUid);
        schedule.setStartDate(scheduleStartDate);
        schedule.setEndDate(scheduleEndDate);
        schedule.setDaysRun(scheduleDayRuns);
        schedule.setStpIndicator(stpIndicator);
        schedule.setTrainStatus(trainStatus);
        schedule.setBankHolidayRuns(bankHolidayRunning);
        if (scheduleSegment != null) {
            schedule.setTrainCategory(scheduleSegment.getTrainCategory());
            schedule.setSignalHeadcode(scheduleSegment.getSignallingId());
            schedule.setOperator(atocCode);
            schedule.setRetailHeadcode(scheduleSegment.getHeadCode());
            schedule.setTrainServiceCode(scheduleSegment.getTrainServiceCode());
            schedule.setPortionId(scheduleSegment.getBusinessSector());
            schedule.setPowerType(scheduleSegment.getPowerType());
            schedule.setTimingLoad(scheduleSegment.getTimingLoad());
            schedule.setPlannedSpeed(scheduleSegment.getSpeed());
            schedule.setOperatingCharacteristics(scheduleSegment.getOperatingCharacteristics());
            schedule.setFirstClass(scheduleSegment.getTrainClass());
            schedule.setSleeper(scheduleSegment.getSleepers());
            schedule.setReservations(scheduleSegment.getReservations());
            schedule.setCatering(scheduleSegment.getCateringCode());
            if (scheduleSegment.getLocations() != null && !scheduleSegment.getLocations().isEmpty()) {
                //Convert the entries
                for (ScheduleLocation location : scheduleSegment.getLocations()) {
                    ScheduleDetail detail = new ScheduleDetail();
                    detail.setLocation(location.getTiplocCode());
                    detail.setLocationInstance(location.getTiplocInstance());
                    detail.setArrivalTime(ScheduleUtil.parseTime(location.getArrivalTime()));
                    detail.setDepartureTime(ScheduleUtil.parseTime(location.getDepartureTime()));
                    detail.setPassTime(ScheduleUtil.parseTime(location.getPassTime()));
                    detail.setPublicArrivalTime(ScheduleUtil.parseTime(location.getPublicArrivalTime()));
                    detail.setPublicDepartureTime(ScheduleUtil.parseTime(location.getPublicDepartureTime()));
                    detail.setPlatform(location.getPlatform());
                    detail.setPath(location.getPath());
                    detail.setLine(location.getLine());
                    detail.setEngineeringAllowance(ScheduleUtil.parseAllowance(location.getEngineeringAllowance()));
                    detail.setPerformanceAllowance(ScheduleUtil.parseAllowance(location.getPerformanceAllowance()));
                    detail.setPathingAllowance(ScheduleUtil.parseAllowance(location.getPathingAllowance()));
                    schedule.getDetailList().add(detail);
                }
            }
        }
        return schedule;
    }

}
