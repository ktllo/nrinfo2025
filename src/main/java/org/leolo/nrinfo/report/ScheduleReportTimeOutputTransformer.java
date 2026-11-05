package org.leolo.nrinfo.report;

public class ScheduleReportTimeOutputTransformer extends OutputTransformer {

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
        if (!"html".equalsIgnoreCase(format)) {
            return oldData;
        }
        //Split it into 3 parts
        if (oldData == null || oldData.isBlank()) {
            return oldData;
        }
        if ("pass".equalsIgnoreCase(oldData)) {
            return "pass";
        }
        String[] data = oldData.split(":");
        if (data[2].equals("00")) {
            return String.format("%s:%s", data[0], data[1]);
        } else {
            return String.format("%s:%s<span style='font-size:smaller;'>:%s</span>", data[0], data[1], data[2]);
        }
    }

    /**
     * Returns the CSS class to be applied to HTML report output.
     * <p>
     * Subclasses may override this method to provide a CSS class based on
     * the supplied data. The default implementation returns an empty string.
     *
     * @param data the report data for which the CSS class is required
     * @return the CSS class to apply, or an empty string if no CSS class is
     * required
     */
    @Override
    public String getHtmlCssClass(String data) {
        if ("pass".equalsIgnoreCase(data)) {
            return "schedule-pass";
        }
        return "";
    }
}
