package org.leolo.nrinfo.model.report;

import java.util.Comparator;

public record ReportParameterOption (String displayValue, String mappedValue, int optionPosition) {

    public static final Comparator<ReportParameterOption> COMPARATOR = Comparator
            .comparing(ReportParameterOption::optionPosition)
            .thenComparing(ReportParameterOption::displayValue)
            .thenComparing(ReportParameterOption::mappedValue);

}
