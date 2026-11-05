package org.leolo.nrinfo.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class TimePeriodTransformer extends OutputTransformer {


    private static final Logger log = LoggerFactory.getLogger(TimePeriodTransformer.class);


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
        if (oldData == null || oldData.isBlank()) {
            return oldData;
        }
        Duration duration;
        try {
            duration = Duration.ofSeconds(Long.parseLong(oldData));
        } catch (NumberFormatException e) {
            log.error("Unable to parse duration: {}", oldData);
            return oldData;
        }
        StringBuilder result = new StringBuilder();
        if (duration.toDays() > 0) {
            result.append(duration.toDays()).append(" days ");
        }
        if (duration.toHours() > 0) {
            if (duration.toHoursPart() < 10) {
                result.append("&nbsp;");
            }
            result.append(duration.toHoursPart()).append(" hours ");
        }
        if (duration.toMinutes() > 0) {
            if (duration.toMinutesPart() < 10) {
                result.append("&nbsp;");
            }
            result.append(duration.toMinutesPart()).append(" minutes ");
        }
        if (duration.toSeconds() > 0) {
            if (duration.toSecondsPart() < 10) {
                result.append("&nbsp;");
            }
            result.append(duration.toSecondsPart()).append(" seconds");
        }
        return result.toString().strip();
    }
}
