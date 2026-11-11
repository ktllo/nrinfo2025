package org.leolo.nrinfo.model.report;

import java.util.Comparator;

public record Report(
        String reportId,
        String reportName,
        String reportMode,
        String reportExecName,
        String htmlStyle,
        boolean skipPermissionCheck,
        boolean hidden
) {
    public static Comparator<Report> reportComparator = Comparator.comparing(Report::reportId);

    public Report(String reportId, String reportName, String reportMode, String reportExecName, String htmlStyle) {
        this(reportId, reportName, reportMode, reportExecName, htmlStyle, false, false);
    }
}
