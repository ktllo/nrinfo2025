package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewScheduleSegment {
    @JsonProperty("traction_class") private String tractionClass;
    @JsonProperty("uic_code") private String uicCode;
}
