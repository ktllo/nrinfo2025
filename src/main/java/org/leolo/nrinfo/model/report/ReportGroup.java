package org.leolo.nrinfo.model.report;

import java.util.ArrayList;
import java.util.List;

public record ReportGroup(String reportGroupId, String reportGroupName, List<Report> reports) {
    public ReportGroup(String reportGroupId, String reportGroupName) {
        this(reportGroupId, reportGroupName, new ArrayList<>());
    }
}
