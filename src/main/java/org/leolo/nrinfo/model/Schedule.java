package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

@Getter
@Setter
@ToString
public class Schedule {
    private UUID scheduleUuid;
    private String trainUid;
    private Date startDate;
    private Date endDate;
    private String daysRun;
    private String stpIndicator;
    private String trainStatus;
    private String bankHolidayRuns;
    private String trainCategory;
    private String signalHeadcode;
    private String operator;
    private String retailHeadcode;
    private String trainServiceCode;
    private String portionId;
    private String powerType;
    private String timingLoad;
    private int plannedSpeed;
    private String operatingCharacteristics;
    private String firstClass;
    private String sleeper;
    private String reservations;
    private String catering;

    private List<ScheduleDetail> detailList = new Vector<>();

    public void setDetailList(List<ScheduleDetail> detailList) {
        throw new RuntimeException("oprtation not permitted");
    }

}
