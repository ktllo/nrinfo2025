package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.util.ScheduleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VstpSchedule {

    @JsonIgnore
    private Logger log = LoggerFactory.getLogger(VstpSchedule.class);

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
        schedule.setStpIndicator(stpIndicator);
        if (this.scheduleSegment == null || this.scheduleSegment.isEmpty()) {
            log.debug("scheduleSegment is null or empty, this is probably a delete transaction");
            return schedule;
        }
        VstpScheduleSegment scheduleSegment = this.scheduleSegment.getFirst();
        schedule.setSignalHeadcode(scheduleSegment.getSignallingId());
        schedule.setOperator(scheduleSegment.getAtocCode());
        schedule.setTrainServiceCode(scheduleSegment.getTrainServiceCode());
        schedule.setTrainCategory(scheduleSegment.getTrainCategory());
        if (scheduleSegment.getSpeed() != null && !scheduleSegment.getSpeed().isBlank()) {
            String speed = scheduleSegment.getSpeed();
            while (speed.startsWith("0")) {
                //Remove 0s in front of the string
                speed = speed.substring(1);
            }
            int plannedSpeed = 0;
            try {
                plannedSpeed = Integer.parseInt(speed);
            } catch (NumberFormatException e) {
                log.error("Parsing speed {} is not a number", scheduleSegment.getSpeed(), e);
            }
            schedule.setPlannedSpeed(plannedSpeed);
        }
        schedule.setPowerType(scheduleSegment.getPowerType());
        if (scheduleSegment.getScheduleLocation() == null || scheduleSegment.getScheduleLocation().isEmpty()) {
            throw new IllegalArgumentException("Schedule location is null or empty");
        }
        for (VstpScheduleLocation location : scheduleSegment.getScheduleLocation()) {
            ScheduleDetail scheduleDetail = new ScheduleDetail();
            scheduleDetail.setLocation(location.getTiplocId());
            try {
                scheduleDetail.setArrivalTime(parseLocationTime(location.getScheduledArrivalTime()));
                scheduleDetail.setDepartureTime(parseLocationTime(location.getScheduledDepartureTime()));
                scheduleDetail.setPassTime(parseLocationTime(location.getScheduledPassTime()));
                scheduleDetail.setPublicArrivalTime(parseLocationTime(location.getPublicArrivalTime()));
                scheduleDetail.setPublicDepartureTime(parseLocationTime(location.getPublicDepartureTime()));
                scheduleDetail.setPathingAllowance(ScheduleUtil.parseAllowance(location.getPathingAllowance()));
                scheduleDetail.setPerformanceAllowance(ScheduleUtil.parseAllowance(location.getPerformanceAllowance()));
                scheduleDetail.setEngineeringAllowance(ScheduleUtil.parseAllowance(location.getEngineeringAllowance()));
            } catch (ParseException pe) {
                throw new IllegalArgumentException("Location time could not be parsed", pe);
            }
            scheduleDetail.setPlatform(preprocessSublocation(location.getPlatform()));
            scheduleDetail.setPath(preprocessSublocation(location.getPath()));
            scheduleDetail.setLine(preprocessSublocation(location.getLine()));
            schedule.getDetailList().add(scheduleDetail);
        }
        return schedule;
    }

    private Time parseLocationTime(String time) throws ParseException {
        if (time == null || time.isBlank()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        return new Time(sdf.parse(time).getTime());
    }
    private String preprocessSublocation(String sublocation) {
        if (sublocation == null || sublocation.isBlank()) {
            return null;
        }
        return sublocation.trim();
    }
}
