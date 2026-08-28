package org.leolo.nrinfo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.leolo.nrinfo.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Getter
@Setter
@ToString
public class ScheduleSearch {

    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fromTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date toTime;
    private int pageSize = 0;
    private boolean callOnly = false;
    private boolean publicTimetableOnly = false;
    private String headcode;
    private boolean hideCancelledTrain = true;
    private boolean strictLocationMatch = false;

    @JsonIgnore
    public static final Logger log = LoggerFactory.getLogger(ScheduleSearch.class);


    public void normalize() {
        if (location != null) {
            location = location.trim().toUpperCase();
        }
        if (headcode != null) {
            headcode = headcode.trim().toUpperCase();
        }
        if (fromTime == null) {
            fromTime = new Date();
        }
        fromTime = Date.from(fromTime.toInstant().truncatedTo(ChronoUnit.MINUTES));
        if (toTime == null) {
            Instant instant = fromTime.toInstant();
            //Move the toTime to be 1h ahead of the fromTime, which defaults to current time
            toTime = Date.from(instant.plus(60, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES));
        }
    }

    public void validate(ValidateMode validateMode) {
        if (location == null && headcode == null) {
            throw new ValidationException("At least one of location, headcode is required");
        }
        if (fromTime.after(toTime)) {
            throw new ValidationException("fromTime is after toTime");
        }
        if (headcode != null) {
            //Headcodes must matches /^[0-9][A-Z][0-9]{2}$/
            if (!headcode.matches("^[0-9][A-Z][0-9]{2}$")) {
                throw new ValidationException("headcode does not match the format");
            }
        }
        Instant fromInstant = Instant.ofEpochMilli(fromTime.getTime());
        Instant toInstant = Instant.ofEpochMilli(toTime.getTime());
        Duration duration = Duration.between(fromInstant, toInstant);
        log.info("Search period: {}", duration);
        if (validateMode == ValidateMode.PUBLIC && duration.compareTo(Duration.ofMinutes(60)) > 0) {
            throw new ValidationException("duration cannot be greater than 60 minutes");
        } else if (validateMode == ValidateMode.REGULAR && duration.compareTo(Duration.ofMinutes(180)) > 0) {
            throw new ValidationException("duration cannot be greater than 180 minutes");
        }

        if (!fromInstant.truncatedTo(ChronoUnit.DAYS).equals(toInstant.truncatedTo(ChronoUnit.DAYS))) {
            throw new ValidationException("fromTime and toTime must be on same day");
        }
    }

    public enum ValidateMode {
        PUBLIC,
        REGULAR,
        SUPER;
    }

//    public enum
}
