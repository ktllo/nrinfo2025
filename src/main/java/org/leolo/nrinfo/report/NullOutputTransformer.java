package org.leolo.nrinfo.report;

public class NullOutputTransformer extends OutputTransformer {

    /**
     * Transforms report output data.
     *
     * @param format  the output format
     * @param oldData the original data
     * @return the transformed data, or the original data if no transformation
     * is required
     */
    @Override
    public String transformData(String format, String oldData) {
        if (oldData == null) {
            return "(null)";
        }
        return oldData;
    }
}
