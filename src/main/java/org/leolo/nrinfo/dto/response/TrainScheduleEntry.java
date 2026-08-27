package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainScheduleEntry {
    private String tiplocCode;
    private String locationName;
    private String crsCode;

    //WTT
    private String wttArrivalTime;
    private String wttDepartureTime;
    private String wttPassTime;

    //GBTT
    private String gbttArrivalTime;
    private String gbttDepartureTime;

    //Line and Path
    private String line;
    private String path;
    private String platform;

    //Allowance
    private String pathingAllowance;
    private String performanceAllowance;
    private String engineeringAllowance;

    //Associations
    private Association association;



    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Association {
        private String associationType;
        private TrainScheduleSummary otherTrain;
    }
}
