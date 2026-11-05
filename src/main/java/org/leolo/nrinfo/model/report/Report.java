package org.leolo.nrinfo.model.report;

import java.util.Comparator;

public record Report(String reportId, String reportName, String reportMode, String reportExecName, String htmlStyle) {
    public static Comparator<Report> reportComparator = Comparator.comparing(Report::reportId);
}
