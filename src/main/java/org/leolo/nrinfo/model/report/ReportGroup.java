package org.leolo.nrinfo.model.report;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class ReportGroup {

    private final String reportGroupId;
    private final String reportGroupName;
    private List<Report> reports;
    private final int displayOrder;

    public ReportGroup(String reportGroupId, String reportGroupName) {
        this(reportGroupId, reportGroupName, new ArrayList<>(), 0);
    }

    public ReportGroup(String reportGroupId, String reportGroupName, List<Report> reports) {
        this(reportGroupId, reportGroupName, reports, 0);
    }

    public ReportGroup(String reportGroupId, String reportGroupName, int displayOrder) {
        this(reportGroupId, reportGroupName, new ArrayList<>(), displayOrder);
    }

    public ReportGroup(String reportGroupId, String reportGroupName,
                       List<Report> reports, int displayOrder) {
        this.reportGroupId = reportGroupId;
        this.reportGroupName = reportGroupName;
        this.reports = reports;
        this.displayOrder = displayOrder;
    }

    public void filterReports(Predicate<Report> filter) {
        reports = reports.stream().filter(filter).collect(Collectors.toList());
    }
}