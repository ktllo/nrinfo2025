package org.leolo.nrinfo.report;

/**
 * Base class for report output transformers.
 * <p>
 * Output transformers allow report titles and data to be modified before
 * being included in the generated output. Subclasses may override either
 * {@link #transformTitle(String, String)} or
 * {@link #transformData(String, String)}, or both.
 * <p>
 * Implementations MUST be thread-safe. The same instance of a transformer
 * MAY be reused for multiple report generations, including concurrent
 * executions. Implementations MUST NOT rely on instance state being
 * exclusive to a single report generation.
 * <p>
 * The default implementations return the original values unchanged.
 */
public abstract class OutputTransformer {

    /**
     * Transforms a report output title.
     *
     * @param format the output format
     * @param oldTitle the original title
     * @return the transformed title, or the original title if no transformation
     *         is required
     */
    public String transformTitle(String format, String oldTitle) {
        return oldTitle;
    }

    /**
     * Transforms report output data.
     *
     * @param format the output format
     * @param oldData the original data
     * @return the transformed data, or the original data if no transformation
     *         is required
     */
    public String transformData(String format, String oldData) {
        return oldData;
    }
    /**
     * Returns the CSS class to be applied to HTML report output.
     * <p>
     * Subclasses may override this method to provide a CSS class based on
     * the supplied data. The default implementation returns an empty string.
     *
     * @param data the report data for which the CSS class is required
     * @return the CSS class to apply, or an empty string if no CSS class is
     *         required
     */
    public String getHtmlCssClass(String data) {
        return "";
    }

}
