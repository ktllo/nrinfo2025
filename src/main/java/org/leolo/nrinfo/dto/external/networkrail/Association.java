package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Getter
@Setter
public class Association {

    private static Logger logger = LoggerFactory.getLogger(Association.class);

    @JsonProperty("transaction_type") private String transactionType;
    @JsonProperty("main_train_uid") private String mainTrainUid;
    @JsonProperty("assoc_train_uid") private String assocTrainUid;
    @JsonProperty("assoc_start_date") private Date assocStartDate;
    @JsonProperty("assoc_end_date") private Date assocEndDate;
    @JsonProperty("assoc_days") private String assocDays;
    @JsonProperty("category") private String category;
    @JsonProperty("date_indicator") private String dateIndicator;
    @JsonProperty("location") private String location;
    @JsonProperty("base_location_suffix") private String baseLocationSuffix;
    @JsonProperty("assoc_location_suffix") private String assocLocationSuffix;
    @JsonProperty("diagram_type") private String diagramType = "T";
    @JsonProperty("CIF_stp_indicator") private String stpIndicator;

    public ScheduleAssociation toModel() {
        ScheduleAssociation a = new ScheduleAssociation();
        a.setBaseUid(mainTrainUid);
        a.setAssocUid(assocTrainUid);
        a.setStartDate(assocStartDate);
        a.setEndDate(assocEndDate);
        a.setAssocDays(assocDays);
        a.setStpIndicator(stpIndicator);
        switch (dateIndicator) {
            case " ":
            case "":
            case null:
            case "S":
                a.setAssocDate(0);
                break;
            case "N":
                a.setAssocDate(1);
                break;
            case "P":
                a.setAssocDate(-1);
                break;
            default:
                logger.warn("Unknown date_indicator {}", dateIndicator);
        }
        a.setAssocLocation(location);
        a.setBaseSuffix(baseLocationSuffix);
        a.setAssocSuffix(assocLocationSuffix);
        a.setAssocCategory(category);
        return a;
    }
}
