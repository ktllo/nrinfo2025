package org.leolo.nrinfo.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.leolo.nrinfo.enums.SortOrder;
import org.leolo.nrinfo.exception.ValidationException;

import java.util.Date;

@ToString
@Getter
@Setter
public class JobSearch {

    private Date fromDate;
    private Date toDate;
    private String username;
    private int pageSize = 0;
    private int skip = 0;
    private String sortField;
    private SortOrder sortOrder;
    @JsonIgnore private int userId;

    public void validate(){
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new ValidationException("fromDate is after toDate");
        }
        if (pageSize == 0) {
            pageSize = 10;
        } else if (pageSize < 0 || pageSize > 100) {
            throw new ValidationException("Page size must be between 1 and 100");
        }
        if (skip < 0) {
            throw new ValidationException("record skipped must be greater than 0");
        }
        if (sortField == null) {
            sortField = "jobId";
        } else if (
                !sortField.equalsIgnoreCase("jobId") &&
                        !sortField.equalsIgnoreCase("jobClass") &&
                        !sortField.equalsIgnoreCase("submittedTime") &&
                        !sortField.equalsIgnoreCase("startTime") &&
                        !sortField.equalsIgnoreCase("endTime") &&
                        !sortField.equalsIgnoreCase("status")
        ) {
            throw new ValidationException("sort field must be one of jobId, jobClass, submittedTime, startTime, endTime, status");
        }
        if (sortOrder == null) {
            sortOrder = SortOrder.ASC;
        }
    }

}
