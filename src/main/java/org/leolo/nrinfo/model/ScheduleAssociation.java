package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ScheduleAssociation {
    private String baseUid;
    private String assocUid;
    private Date startDate;
    private Date endDate;
    private String assocDays;
    private String stpIndicator;

    private int assocDate;
    private String assocLocation;
    private String baseSuffix;
    private String assocSuffix;
    private String assocCategory;
    private String assocType;

    private Date createdDate;
}
