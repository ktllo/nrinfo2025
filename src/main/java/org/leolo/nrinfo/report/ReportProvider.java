package org.leolo.nrinfo.report;

import org.leolo.nrinfo.model.report.ExecutionResult;
import org.leolo.nrinfo.model.report.ReportParameters;

import java.util.List;

public abstract class ReportProvider {

    public abstract ExecutionResult executeReport(List<ReportParameters> parametersList);

}
