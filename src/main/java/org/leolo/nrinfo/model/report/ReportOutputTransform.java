package org.leolo.nrinfo.model.report;

import java.util.Comparator;

public record ReportOutputTransform(
        String reportId,
        int transformSequence,
        int outputPosition,
        String transformClass) {

    public static final Comparator<ReportOutputTransform> COMPARATOR =
            Comparator.comparing(ReportOutputTransform::reportId)
                    .thenComparing(ReportOutputTransform::transformSequence);

}
