package org.leolo.nrinfo.report;

import org.leolo.nrinfo.model.report.GeneratedReport;
import org.leolo.nrinfo.model.report.ReportParameters;

import java.util.List;

public abstract class SpecialReportProvider {

    public abstract GeneratedReport generateReport(List<ReportParameters> parameters);

}
