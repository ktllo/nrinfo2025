package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Tiploc {

    @JsonProperty("transaction_type") private String transactionType;
    @JsonProperty("tiploc_code") private String tiplocCode;
    @JsonProperty("nalco") private String nalco;
    @JsonProperty("stanox") private String stanox;
    @JsonProperty("crs_code") private String crsCode;
    @JsonProperty("description") private String description;
    @JsonProperty("tps_description") private String tpsDescription;

    public org.leolo.nrinfo.model.Tiploc toModel() {
        return new org.leolo.nrinfo.model.Tiploc(
                tiplocCode,
                nalco,
                stanox,
                crsCode,
                description,
                tpsDescription
        );
    }

}
