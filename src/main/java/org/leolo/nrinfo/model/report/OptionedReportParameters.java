package org.leolo.nrinfo.model.report;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class OptionedReportParameters extends ReportParameters {

    private final List<ReportParameterOption> options = new ArrayList<>();

    public OptionedReportParameters(String reportId, int position, String parameterName, String parameterType, boolean required, String validationGroup) {
        super(reportId, position, parameterName, parameterType, required, validationGroup);
    }

    public OptionedReportParameters(String reportId, int position, String parameterName, String parameterType, boolean required, String validationGroup, String parameterValue) {
        super(reportId, position, parameterName, parameterType, required, validationGroup, parameterValue);
    }

    public OptionedReportParameters(ReportParameters reportParameters) {
        //Copy over the value
        super(
                reportParameters.getReportId(),
                reportParameters.getPosition(),
                reportParameters.getParameterName(),
                reportParameters.getParameterType(),
                reportParameters.isRequired(),
                reportParameters.getValidationGroup(),
                reportParameters.getParameterValue()
        );
        setValidationMessage(reportParameters.getValidationMessage());
        setDisplayedValue(reportParameters.getDisplayedValue());
    }

    public int getOptionCount() {
        return options.size();
    }
}
