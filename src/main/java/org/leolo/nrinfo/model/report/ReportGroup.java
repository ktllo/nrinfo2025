package org.leolo.nrinfo.model.report;

import java.util.ArrayList;
import java.util.List;

public record ReportGroup(String reportGroupId, String reportGroupName, List<Report> reports, int displayOrder) {
    public ReportGroup(String reportGroupId, String reportGroupName) {
        this(reportGroupId, reportGroupName, new ArrayList<>());
    }

    public ReportGroup(String reportGroupId, String reportGroupName, List<Report> reports) {
        this(reportGroupId, reportGroupName, reports, 0);
    }

    public ReportGroup(String reportGroupId, String reportGroupName,  int displayOrder) {
        this(reportGroupId, reportGroupName, new ArrayList<>(), displayOrder);
    }
}
